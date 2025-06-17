package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class UserRegistered implements DomainEvent {

  private final UserId userId;
  private final Email email;
  private final Instant occurredAt;

  public UserRegistered(UserId userId, Email email) {
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.email = Objects.requireNonNull(email, "email must not be null");
    this.occurredAt = Instant.now();
  }

  public static UserRegistered of(UserId userId, Email email) {
    return new UserRegistered(userId, email);
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
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
        + ", occurredAt=" + occurredAt
        + '}';
  }
}
