package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class PasswordChanged extends AbstractDomainEvent {
  private final UserId userId;

  public PasswordChanged(UserId userId, Instant occurredAt) {
    super(occurredAt);
    this.userId = requireNonNull(userId, "userId");
  }

  /**
   * Creates a new PasswordChanged event.
   *
   * @param userId the ID of the user whose password was changed
   * @param occurredAt the timestamp when the event occurred
   * @return a new PasswordChanged event
   */
  public static PasswordChanged createEvent(UserId userId, Instant occurredAt) {
    return new PasswordChanged(userId, occurredAt);
  }

  /**
   * Creates a new PasswordChanged event with current timestamp.
   *
   * @deprecated Use {@link #createEvent(UserId, Instant)} with explicit timestamp for deterministic behavior
   * @param userId the ID of the user whose password was changed
   * @return a new PasswordChanged event
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public static PasswordChanged createEvent(UserId userId) {
    return createEvent(userId, Instant.now());
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
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
