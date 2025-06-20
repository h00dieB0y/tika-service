package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.Objects;

public class UserRegistered extends AbstractDomainEvent {

  private final UserId userId;
  private final Email email;

  public UserRegistered(UserId userId, Email email) {
    super();
    this.userId = requireNonNull(userId, "userId");
    this.email = requireNonNull(email, "email");
  }

  /**
   * Creates a new UserRegistered event.
   *
   * @param userId the ID of the user that was registered
   * @param email the email of the registered user
   * @return a new UserRegistered event
   */
  public static UserRegistered of(UserId userId, Email email) {
    return new UserRegistered(userId, email);
  }

  public UserId getUserId() {
    return userId;
  }

  public Email getEmail() {
    return email;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof UserRegistered that)) {
      return false;
    }

    return Objects.equals(userId, that.userId)
      && Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, email);
  }

  @Override
  public String toString() {
    return "UserRegistered{"
        + "userId=" + userId
        + ", email=" + email
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
