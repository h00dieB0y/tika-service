package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class PasswordChanged implements  DomainEvent {
  private final UserId userId;
  private final Instant occurredAt;

  public PasswordChanged(UserId userId) {
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.occurredAt = Instant.now();
  }

  public static PasswordChanged of(UserId userId) {
    return new PasswordChanged(userId);
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
  }

  public UserId getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PasswordChanged that)) {
      return false;
    }

    return Objects.equals(this.userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId);
  }

  @Override
  public String toString() {
    return "PasswordChanged{"
        + "userId=" + userId
        + ", occurredAt=" + occurredAt
        + '}';
  }
}
