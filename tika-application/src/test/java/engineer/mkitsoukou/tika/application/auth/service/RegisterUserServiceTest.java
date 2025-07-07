package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.RegisterUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.UserDto;
import engineer.mkitsoukou.tika.application.auth.port.out.EventPublisherPort;
import engineer.mkitsoukou.tika.application.auth.validator.PasswordPolicyValidator;
import engineer.mkitsoukou.tika.application.shared.ClockPort;
import engineer.mkitsoukou.tika.domain.exception.EmailAlreadyRegisteredException;
import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.event.UserRegistered;
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.PasswordHash;
import engineer.mkitsoukou.tika.domain.model.valueobject.PlainPassword;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import engineer.mkitsoukou.tika.domain.repository.UserRepository;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterUserServiceTest {

  /* ---------- Fakes & stubs ---------- */

  /** Simple in-memory repository safe for concurrent tests. */
  static final class InMemUserRepo implements UserRepository {
    private final Map<String, User> byEmail = new ConcurrentHashMap<>();

    @Override public boolean existsByEmail(Email email) {
      return byEmail.containsKey(email.value());
    }

    @Override
    public long count() {
      return 0;
    }

    @Override
    public Optional<User> findById(UserId userId) {
      return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(Email email) {
      return Optional.empty();
    }

    @Override
    public List<User> findAll() {
      return List.of();
    }

    @Override public Optional<User> save(User user) {
      byEmail.put(user.getEmail().value(), user);
      return Optional.of(user);
    }

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

    /* helper */ int size() { return byEmail.size(); }
  }

  /** Minimal hasher producing valid "$2..." strings to satisfy PasswordHash VO. */
  static final class StubHasher implements PasswordHasher {
    @Override public PasswordHash hash(PlainPassword pw) {
      return new PasswordHash("$2" + pw.clearText());   // bcrypt prefix required by VO
    }
    @Override
    public boolean match(PlainPassword pw, PasswordHash h) {
      return h.hash().equals("$2" + pw.clearText());
    }
  }

  /* ---------- Shared test objects ---------- */

  private InMemUserRepo repo;
  private PasswordPolicyValidator policy;
  private PasswordHasher hasher;
  private EventPublisherPort publisher;
  private ClockPort clock;
  private RegisterUserService service;

  @BeforeEach
  void setUp() {
    repo      = new InMemUserRepo();
    policy    = new PasswordPolicyValidator();   // default rules: entropy & repeat
    hasher    = new StubHasher();
    publisher = Mockito.mock(EventPublisherPort.class);
    clock     = new ClockPort() {
      final Instant fixed = Instant.parse("2025-07-07T12:00:00Z");
      @Override public Instant now()  { return fixed; }
      @Override public java.time.ZoneId zone() { return ZoneOffset.UTC; }
    };
    service   = new RegisterUserService(repo, policy, hasher, publisher, clock);
  }

  /* ------------- Z O M B I E S tests ------------- */

  /* Z – Zero / invalid input */
  @Test
  void blankPasswordShouldFail() {
    RegisterUserCommand cmd = new RegisterUserCommand("bob@example.com", "");
    assertThatThrownBy(() -> service.execute(cmd))
      .isInstanceOf(InvalidPasswordException.class);
  }

  /* O – One (happy path) */
  @Test
  void happyPathShouldPersistUserAndReturnDto() {
    RegisterUserCommand cmd = new RegisterUserCommand("alice@example.com", "Str0ng@Pwd1");
    UserDto dto = service.execute(cmd);

    // repository size
    assertThat(repo.size()).isEqualTo(1);
    // DTO mapping
    assertThat(dto.email()).isEqualTo("alice@example.com");
    // event published once
    verify(publisher, times(1)).publish(isA(UserRegistered.class));
  }

  /* M – Many / concurrent registrations */
  @Test
  void parallelRegistrationsWithUniqueEmails() {
    IntStream.range(0, 10).parallel().forEach(i -> {
      RegisterUserCommand c = new RegisterUserCommand(
        "user" + i + "@example.com", "C0mpl3xPwd@" + i);
      service.execute(c);
    });
    assertThat(repo.size()).isEqualTo(10);
  }

  /* B – Boundary (length 8 passes, 7 fails) */
  @Test
  void passwordLengthBoundary() {
    // length 7 => should fail
    RegisterUserCommand seven = new RegisterUserCommand(
      "mini@example.com", "Aa1!a1!");
    assertThatThrownBy(() -> service.execute(seven))
      .isInstanceOf(InvalidPasswordException.class);

    // length 8 => should pass
    RegisterUserCommand eight = new RegisterUserCommand(
      "normal@example.com", "Aa1!aa1!");
    assertThatCode(() -> service.execute(eight)).doesNotThrowAnyException();
  }

  /* I – Interface / mocking (verify event published) */
  @Test
  void shouldPublishUserRegisteredEvent() {
    RegisterUserCommand cmd = new RegisterUserCommand("eve@example.com", "Sup3r@Pwd!");
    service.execute(cmd);

    verify(publisher).publish(argThat(ev -> ev instanceof UserRegistered
      && ((UserRegistered) ev).getEmail().equals("eve@example.com")));
  }

  /* E – Exception: duplicate email */
  @Test
  void duplicateEmailShouldThrowSpecificException() {
    // first registration succeeds
    service.execute(new RegisterUserCommand("dup@example.com", "Secur3@Pwd!"));
    // second one duplicates
    RegisterUserCommand dup = new RegisterUserCommand("dup@example.com", "Secur3@Pwd!");
    assertThatThrownBy(() -> service.execute(dup))
      .isInstanceOf(EmailAlreadyRegisteredException.class);
    // repo size unchanged
    assertThat(repo.size()).isEqualTo(1);
  }

  /* S – Simple: DTO fields */
  @Test
  void dtoShouldContainExpectedValues() {
    RegisterUserCommand cmd = new RegisterUserCommand("simple@example.com", "Simpl3r@Pwd!");
    UserDto dto = service.execute(cmd);

    assertThat(dto.id()).isNotBlank();
    assertThat(dto.email()).isEqualTo("simple@example.com");
  }
}
