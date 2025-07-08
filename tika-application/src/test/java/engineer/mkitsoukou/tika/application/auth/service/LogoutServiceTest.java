package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.LogoutCommand;
import engineer.mkitsoukou.tika.application.auth.exception.InvalidCredentialsException;
import engineer.mkitsoukou.tika.application.auth.model.JwtClaims;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtValidatorPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RefreshTokenStorePort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


class LogoutServiceTest {

  /* ───────────── fakes / stubs ───────────── */

  private static final String USER_ID = "uid-123";
  private static final String AT = "AT-token";

  /* ───────────── constants ───────────── */
  private static final String JTI = "jti-abc";
  private static final Instant NOW = Instant.parse("2025-07-07T12:00:00Z");
  private TokenBlacklistPort blacklist;
  private InMemRefreshTokenStore store;

  /* ───────────── collaborators ───────────── */
  private LogoutService service;

  @BeforeEach
  void init() {

    /* Stub validator – validates AT and returns claims */
    JwtValidatorPort validator = mock(JwtValidatorPort.class);
    when(validator.validateAccessToken(AT))
      .thenReturn(new JwtClaims(USER_ID, JTI, Instant.now(),
        Instant.now().plusSeconds(900), Set.of()));
    when(validator.validateAccessToken("expired"))
      .thenThrow(new InvalidCredentialsException());
    when(validator.validateAccessToken(""))
      .thenThrow(new InvalidCredentialsException());

    blacklist = spy(new InMemBlacklist());
    store = new InMemRefreshTokenStore();

    service = new LogoutService(validator, blacklist, store);
  }

  /* ───────────── Z – blank input ───────────── */
  @Test
  void blankTokenShouldFail() {
    assertThatThrownBy(() -> service.execute(new LogoutCommand("")))
      .isInstanceOf(InvalidCredentialsException.class);
  }

  /* ───────────── O – happy path ───────────── */
  @Test
  void happyPathBlacklistsAndRevokes() {
    service.execute(new LogoutCommand(AT));

    assertThat(((InMemBlacklist) blacklist).set).contains(JTI);
    assertThat(store.revokedUsers).contains(USER_ID);
  }

  /* ───────────── M – idempotent parallel ───────────── */
  @Test
  void parallelLogoutsAreIdempotent() {
    IntStream.range(0, 50).parallel().forEach(i ->
      assertThatCode(() -> service.execute(new LogoutCommand(AT))).doesNotThrowAnyException());

    assertThat(((InMemBlacklist) blacklist).set).hasSize(1);
  }

  @Test
  void expiredTokenShouldFail() {
    assertThatThrownBy(() -> service.execute(new LogoutCommand("expired")))
      .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void blacklistCalledExactlyOnce() {
    service.execute(new LogoutCommand(AT));

    verify(blacklist, times(1)).blacklist(JTI);
  }

  @Test
  void tokenWithoutRtEntryStillBlacklisted() {
    // Arrange: another user who DOES have an RT in the store
    store.store("other-user", "RT-other", NOW.plusSeconds(3600));

    // Act: logout for USER_ID whose RT is NOT in the store
    service.execute(new LogoutCommand(AT));

    // Assert – JTI is black-listed
    assertThat(((InMemBlacklist) blacklist).set).contains(JTI);

    // Only USER_ID is revoked
    assertThat(store.revokedUsers).containsExactly(USER_ID);

    // Other user’s RT still valid → not revoked accidentally
    assertThat(store.isValid("other-user", "RT-other")).isTrue();
  }

  @Test
  void secondLogoutIdempotent() {
    service.execute(new LogoutCommand(AT));
    assertThatCode(() -> service.execute(new LogoutCommand(AT))).doesNotThrowAnyException();
  }

  /**
   * Simple blacklist backed by a Set.
   */
  static final class InMemBlacklist implements TokenBlacklistPort {
    final Set<String> set = ConcurrentHashMap.newKeySet();

    @Override
    public boolean isBlacklisted(String jti) {
      return set.contains(jti);
    }

    @Override
    public void blacklist(String jti) {
      set.add(jti);
    }
  }

  /**
   * In-mem refresh-token store; revoke records calls.
   */
  static final class InMemRefreshTokenStore implements RefreshTokenStorePort {
    final Map<String, String> current = new ConcurrentHashMap<>();
    final Set<String> revokedUsers = ConcurrentHashMap.newKeySet();

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
      current.remove(userId);
      revokedUsers.add(userId);
    }
  }
}
