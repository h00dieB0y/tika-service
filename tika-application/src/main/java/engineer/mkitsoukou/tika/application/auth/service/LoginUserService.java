package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.LoginUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.auth.exception.InvalidCredentialsException;
import engineer.mkitsoukou.tika.application.auth.exception.UserInactiveException;
import engineer.mkitsoukou.tika.application.auth.model.AuthSubject;
import engineer.mkitsoukou.tika.application.auth.port.in.LoginUserUseCase;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtIssuerPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RateLimiterPort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;
import engineer.mkitsoukou.tika.application.shared.ClockPort;
import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.PlainPassword;
import engineer.mkitsoukou.tika.domain.repository.UserRepository;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import java.util.Objects;
import java.util.stream.Collectors;

public class LoginUserService implements LoginUserUseCase {
  private final UserRepository userRepo;
  private final PasswordHasher hasher;
  private final JwtIssuerPort jwtIssuer;
  private final TokenBlacklistPort blacklist;
  private final RateLimiterPort rateLimiter;
  private final ClockPort clock;

  public LoginUserService(
      UserRepository userRepo,
      PasswordHasher hasher,
      JwtIssuerPort jwtIssuer,
      TokenBlacklistPort blacklist,
      RateLimiterPort rateLimiter,
      ClockPort clock
  ) {
    this.userRepo = Objects.requireNonNull(userRepo, "UserRepository must not be null");
    this.hasher = Objects.requireNonNull(hasher, "PasswordHasher must not be null");
    this.jwtIssuer = Objects.requireNonNull(jwtIssuer, "JwtIssuerPort must not be null");
    this.blacklist = Objects.requireNonNull(blacklist, "TokenBlacklistPort must not be null");
    this.rateLimiter = Objects.requireNonNull(rateLimiter, "RateLimiterPort must not be null");
    this.clock = Objects.requireNonNull(clock, "ClockPort must not be null");
  }

  @Override
  public AuthTokensDto execute(LoginUserCommand command) {
    Objects.requireNonNull(command, "LoginUserCommand must not be null");

    rateLimiter.checkLoginAllowed(command.email());

    Email email = new Email(command.email());
    User user = userRepo.findByEmail(email)
        .orElseThrow(InvalidCredentialsException::new);

    if (!user.isActive()) {
      throw new UserInactiveException();
    }

    PlainPassword plain = new PlainPassword(command.password());

    if (!hasher.matches(plain, user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    AuthSubject subject = new AuthSubject(
        user.getId().value().toString(),
        user.getRoles().stream()
          .map(r -> r.getRoleId().value().toString())
          .collect(Collectors.toSet()));

    AuthTokensDto tokens = jwtIssuer.issueTokens(subject, clock.now());


    if (blacklist.isBlacklisted(tokens.accessToken())) {
      throw new InvalidCredentialsException();
    }

    rateLimiter.recordSuccessfulLogin(command.email());

    return tokens;
  }
}
