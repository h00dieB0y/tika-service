package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.RefreshTokenCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.auth.exception.InvalidCredentialsException;
import engineer.mkitsoukou.tika.application.auth.model.AuthSubject;
import engineer.mkitsoukou.tika.application.auth.model.JwtClaims;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtIssuerPort;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtValidatorPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RefreshTokenStorePort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;
import engineer.mkitsoukou.tika.application.shared.ClockPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

  /* ──────────────────────  fakes / stubs  ─────────────────────────── */

  private static final String USER1_ID = "uid-1";
  private static final String USER2_ID = "uid-2";

  /* ──────────────────────  constants  ─────────────────────────────── */
  private static final String VALID_RT1 = "RT-uid-1";
  private static final String VALID_RT2 = "RT-uid-2";
  private static final String JTI_OLD = "jti-old";
  private static final Instant NOW = Instant.parse("2025-07-07T12:00:00Z");
  private JwtValidatorPort validator;
  private JwtIssuerPort issuer;

  /* ──────────────────────  collaborators  ─────────────────────────── */
  private RefreshTokenStorePort store;
  private TokenBlacklistPort blacklist;
  private ClockPort clock;
  private RefreshTokenService service;

  @BeforeEach
  void init() {

    /* Stub validator: throws if token equals \"expired\"; else returns claims */
    validator = mock(JwtValidatorPort.class);
    when(validator.validateRefreshToken(VALID_RT1))
      .thenReturn(new JwtClaims(USER1_ID, JTI_OLD, NOW.minusSeconds(60),
        NOW.plus(Duration.ofDays(10)), Set.of()));
    when(validator.validateRefreshToken(VALID_RT2))
      .thenReturn(new JwtClaims(USER2_ID, JTI_OLD, NOW.minusSeconds(60),
        NOW.plus(Duration.ofDays(10)), Set.of()));
    when(validator.validateRefreshToken("expired"))
      .thenThrow(new InvalidCredentialsException());
    when(validator.validateRefreshToken(""))
      .thenThrow(new InvalidCredentialsException());

    /* Spy issuer to verify invocation */
    issuer = spy(new JwtIssuerPort() {
      @Override
      public AuthTokensDto issueTokens(AuthSubject sub, Instant now) {
        String at = "AT-" + sub.userId();
        String rt = "RT-" + sub.userId() + "-NEW";
        return new AuthTokensDto(at, rt, now.plus(Duration.ofMinutes(15)));
      }
    });

    store = new InMemRtStore();
    blacklist = new InMemBlacklist();
    clock = () -> NOW;

    /* Seed store with valid RTs */
    store.store(USER1_ID, VALID_RT1, NOW.plus(Duration.ofDays(10)));
    store.store(USER2_ID, VALID_RT2, NOW.plus(Duration.ofDays(10)));

    service = new RefreshTokenService(
      validator, issuer, store, blacklist, clock);
  }

  @Test
  void blankTokenShouldFail() {
    assertThatThrownBy(() -> service.execute(new RefreshTokenCommand("")))
      .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void happyPathReturnsNewTokens() {
    AuthTokensDto dto = service.execute(new RefreshTokenCommand(VALID_RT1));

    assertThat(dto.accessToken()).isEqualTo("AT-" + USER1_ID);
    assertThat(dto.refreshToken()).isEqualTo("RT-" + USER1_ID + "-NEW");
    assertThat(dto.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(15)));

    verify(issuer).issueTokens(argThat(sub -> sub.userId().equals(USER1_ID)), eq(NOW));
    assertThat(blacklist.isBlacklisted(JTI_OLD)).isTrue();
    assertThat(store.isValid(USER1_ID, dto.refreshToken())).isTrue();
    assertThat(store.isValid(USER1_ID, VALID_RT1)).isFalse();  // revoked
  }

  @Test
  void parallelRefreshesForManyUsers() {
    // Setup mocks and data before parallel execution
    IntStream.range(0, 10).forEach(i -> {
      String uid = "par-" + i;
      String rt = "RT-" + uid;
      store.store(uid, rt, NOW.plusSeconds(3600));

      when(validator.validateRefreshToken(rt)).thenReturn(
        new JwtClaims(uid, "jti-" + i, NOW.minusSeconds(30),
          NOW.plusSeconds(3600), Set.of()));
    });

    // Execute in parallel after setup is complete
    IntStream.range(0, 10).parallel().forEach(i -> {
      String uid = "par-" + i;
      String rt = "RT-" + uid;

      assertThatCode(() -> service.execute(new RefreshTokenCommand(rt)))
        .doesNotThrowAnyException();
    });
  }

  @Test
  void expiredRefreshTokenShouldFail() {
    assertThatThrownBy(() -> service.execute(new RefreshTokenCommand("expired")))
      .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void issuerAndValidatorAreInvoked() {
    service.execute(new RefreshTokenCommand(VALID_RT2));

    verify(validator).validateRefreshToken(VALID_RT2);
    verify(issuer).issueTokens(any(AuthSubject.class), eq(NOW));
  }

  @Test
  void tokenNotInStoreShouldFail() {
    // VALID_RT1 revoked first
    store.revoke(USER1_ID, VALID_RT1);

    assertThatThrownBy(() -> service.execute(new RefreshTokenCommand(VALID_RT1)))
      .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void dtoFieldsNonNull() {
    AuthTokensDto dto = service.execute(new RefreshTokenCommand(VALID_RT2));
    assertThat(dto.accessToken()).isNotBlank();
    assertThat(dto.refreshToken()).isNotBlank();
    assertThat(dto.expiresAt()).isAfter(NOW);
  }

  static final class InMemRtStore implements RefreshTokenStorePort {
    private final Map<String, String> current = new ConcurrentHashMap<>();

    @Override
    public void store(String userId, String rt, Instant exp) {
      current.put(userId, rt);
    }

    @Override
    public boolean isValid(String userId, String rt) {
      return rt.equals(current.get(userId));
    }

    @Override
    public void revoke(String userId, String rt) {
      current.remove(userId, rt);
    }
  }

  static final class InMemBlacklist implements TokenBlacklistPort {
    private final Set<String> set = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isBlacklisted(String jti) {
      return set.contains(jti);
    }

    @Override
    public void blacklist(String jti) {
      set.add(jti);
    }
  }
}
