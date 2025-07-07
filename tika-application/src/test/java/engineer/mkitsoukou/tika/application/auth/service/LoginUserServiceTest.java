package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.LoginUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.auth.exception.InvalidCredentialsException;
import engineer.mkitsoukou.tika.application.auth.exception.TooManyAttemptsException;
import engineer.mkitsoukou.tika.application.auth.exception.UserInactiveException;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtIssuerPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RateLimiterPort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;
import engineer.mkitsoukou.tika.application.shared.ClockPort;
import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.repository.UserRepository;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/* -----------------------------------------------------------------------
   Fakes & stubs
   -------------------------------------------------------------------- */

class InMemUserRepo implements UserRepository {
  private final Map<String, User> byEmail = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findById(UserId userId) {
    return Optional.empty();
  }

  @Override public Optional<User> findByEmail(Email email) {
    return Optional.ofNullable(byEmail.get(email.value()));
  }

  @Override
  public List<User> findAll() {
    return List.of();
  }

  @Override public boolean existsByEmail(Email email) { return byEmail.containsKey(email.value()); }

  @Override
  public long count() {
    return byEmail.size();
  }

  @Override public Optional<User> save(User u) { byEmail.put(u.getEmail().value(), u); return Optional.of(u); }

  @Override
  public boolean delete(User user) {
    return false;
  }

  @Override
  public boolean deleteById(UserId userId) {
    return false;
  }

  @Override
  public boolean existsById(UserId userId) {
    return false;
  }
}

class StubHasher implements PasswordHasher {
  @Override public PasswordHash hash(PlainPassword pw) { return new PasswordHash("$2" + pw.clearText()); }
  @Override public boolean matches(PlainPassword pw, PasswordHash h) { return h.hash().equals("$2" + pw.clearText()); }
}

class StubJwtIssuer implements JwtIssuerPort {
  @Override public AuthTokensDto issueTokens(User user, Instant now) {
    return new AuthTokensDto("AT-" + user.getId(), "RT-" + user.getId(),
      now.plus(Duration.ofMinutes(15)));
  }
}

class NoOpBlacklist implements TokenBlacklistPort {
  @Override public boolean isBlacklisted(String jti) { return false; }
}

class CountingRateLimiter implements RateLimiterPort {
  final Map<String,Integer> attempts = new ConcurrentHashMap<>();
  @Override public void checkLoginAllowed(String email) {
    int c = attempts.merge(email,1,Integer::sum);
    if (c > 5) throw new TooManyAttemptsException(email);
  }

  @Override
  public void recordSuccessfulLogin(String email) {
    attempts.remove(email);
  }
}

/* -----------------------------------------------------------------------
   LoginUserServiceTest
   -------------------------------------------------------------------- */

public class LoginUserServiceTest {

  private InMemUserRepo repo;
  private CountingRateLimiter limiter;
  private PasswordHasher hasher;
  private JwtIssuerPort issuer;
  private TokenBlacklistPort blacklist;
  private ClockPort clock;
  private LoginUserService service;

  private static final String STRONG_PWD = "Sup3r@Pwd!";

  @BeforeEach
  void init() {
    repo      = new InMemUserRepo();
    limiter   = new CountingRateLimiter();
    hasher    = new StubHasher();
    issuer    = spy(new StubJwtIssuer());
    blacklist = new NoOpBlacklist();
    clock     = () -> Instant.parse("2025-07-07T12:00:00Z");
    service   = new LoginUserService(repo, hasher, issuer, blacklist, limiter, clock);

    // prepare one active user in repo
    Email email = new Email("active@example.com");
    User  user  = User.register(email, PlainPassword.of(STRONG_PWD), hasher, clock.now());
    repo.save(user);
  }

  @Test
  void happyPathReturnsTokensAndResetsLimiter() {
    LoginUserCommand cmd = new LoginUserCommand("active@example.com", STRONG_PWD);
    AuthTokensDto tokens = service.execute(cmd);

    assertThat(tokens.accessToken()).startsWith("AT-");
    assertThat(tokens.refreshToken()).startsWith("RT-");
    assertThat(tokens.expiresAt()).isEqualTo(clock.now().plus(Duration.ofMinutes(15)));

    verify(issuer, times(1)).issueTokens(any(), eq(clock.now()));
    assertThat(limiter.attempts).doesNotContainKey("active@example.com");
  }

  @Test
  void parallelLoginsExceedingLimitShouldFail() {
    // first 5 failed attempts
    IntStream.range(0,5).forEach(i ->
      assertThatThrownBy(() ->
        service.execute(new LoginUserCommand("active@example.com", "WrongPassword1!")))
        .isInstanceOf(InvalidCredentialsException.class));

    // 6th attempt should fail due to rate limiting
    assertThatThrownBy(() ->
      service.execute(new LoginUserCommand("active@example.com", "WrongPassword1!")))
      .isInstanceOf(TooManyAttemptsException.class);
  }

  @Test
  void inactiveUserShouldThrow() {
    Email email = new Email("inactive@example.com");
    User  inactive = User.register(email, PlainPassword.of(STRONG_PWD), hasher, clock.now());
    inactive.desactivate(clock.now());
    repo.save(inactive);

    LoginUserCommand cmd = new LoginUserCommand("inactive@example.com", STRONG_PWD);
    assertThatThrownBy(() -> service.execute(cmd))
      .isInstanceOf(UserInactiveException.class);
  }

  @Test
  void jwtIssuerShouldBeInvokedWithCorrectUser() {
    LoginUserCommand cmd = new LoginUserCommand("active@example.com", STRONG_PWD);
    service.execute(cmd);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(issuer).issueTokens(captor.capture(), eq(clock.now()));
    assertThat(captor.getValue().getEmail().value()).isEqualTo("active@example.com");
  }

  @Test
  void wrongPasswordTriggersInvalidCredentials() {
    LoginUserCommand cmd = new LoginUserCommand("active@example.com", "WrongPassword1!");
    assertThatThrownBy(() -> service.execute(cmd))
      .isInstanceOf(InvalidCredentialsException.class);
    assertThat(limiter.attempts).containsEntry("active@example.com", 1);
  }


  @Test
  void dtoFieldsAreNonNull() {
    LoginUserCommand cmd = new LoginUserCommand("active@example.com", STRONG_PWD);
    AuthTokensDto dto = service.execute(cmd);
    assertThat(dto.accessToken()).isNotBlank();
    assertThat(dto.refreshToken()).isNotBlank();
    assertThat(dto.expiresAt()).isAfter(clock.now());
  }
}
