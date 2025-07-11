package engineer.mkitsoukou.tika.application.auth.service;
import engineer.mkitsoukou.tika.application.auth.command.RegisterUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.UserDto;
import engineer.mkitsoukou.tika.application.auth.port.out.EventPublisherPort;
import engineer.mkitsoukou.tika.application.auth.validator.PasswordPolicyValidator;
import engineer.mkitsoukou.tika.application.shared.ClockPort;
import engineer.mkitsoukou.tika.domain.exception.EmailAlreadyRegisteredException;
import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.event.DomainEvent;
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.PlainPassword;
import engineer.mkitsoukou.tika.domain.repository.UserRepository;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import jakarta.transaction.Transactional;

import engineer.mkitsoukou.tika.application.auth.port.in.RegisterUserUseCase;

import java.util.List;
import java.util.Objects;

/**
 * Orchestrates user registration:
 *  1. Soft password policy.
 *  2. Domain value-object validation.
 *  3. Uniqueness check.
 *  4. Aggregate creation (hashing inside {@link User}).
 *  5. Persistence and event publication.
 */
public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepository userRepo;
  private final PasswordPolicyValidator passwordPolicy;
  private final PasswordHasher passwordHasher;
  private final EventPublisherPort events;
  private final ClockPort clock;

public RegisterUserService(
      UserRepository userRepo,
      PasswordPolicyValidator passwordPolicy,
      PasswordHasher passwordHasher,
      EventPublisherPort events,
      ClockPort clock) {
    this.userRepo = Objects.requireNonNull(userRepo, "userRepo must not be null");
    this.passwordPolicy = Objects.requireNonNull(passwordPolicy, "passwordPolicy must not be null");
    this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher must not be null");
    this.events = Objects.requireNonNull(events, "events must not be null");
    this.clock = Objects.requireNonNull(clock, "clock must not be null");
  }

  @Override
  @Transactional
  public UserDto execute(RegisterUserCommand cmd) {

    passwordPolicy.validate(cmd.password());

    Email email = new Email(cmd.email());
    PlainPassword plainPassword = new PlainPassword(cmd.password());

    if (userRepo.existsByEmail(email)) {
      throw new EmailAlreadyRegisteredException(email.value());
    }

    User user = User.register(email, plainPassword, passwordHasher, clock.now());
    userRepo.save(user);

    List<DomainEvent> recorded = user.pullEvents();
    recorded.forEach(events::publish);

    return UserDto.from(user);
  }
}
