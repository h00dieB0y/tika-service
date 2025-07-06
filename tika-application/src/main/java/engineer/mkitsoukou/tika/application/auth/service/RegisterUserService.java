package engineer.mkitsoukou.tika.application.auth.service;
import engineer.mkitsoukou.tika.application.auth.command.RegisterUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.UserDto;
import engineer.mkitsoukou.tika.application.auth.port.out.EventPublisherPort;
import engineer.mkitsoukou.tika.application.auth.validator.PasswordPolicyValidator;
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

public RegisterUserService(
      UserRepository userRepo,
      PasswordPolicyValidator passwordPolicy,
      PasswordHasher passwordHasher,
      EventPublisherPort events) {
    this.userRepo = userRepo;
    this.passwordPolicy = passwordPolicy;
    this.passwordHasher = passwordHasher;
    this.events = events;
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

    User user = User.register(email, plainPassword, passwordHasher);
    userRepo.save(user);

    List<DomainEvent> recorded = user.pullEvents();
    recorded.forEach(events::publish);

    return UserDto.from(user);
  }
}
