package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import java.time.Instant;
import java.util.Objects;

/**
 * Event representing a change in a user's activation status.
 */
public class UserActivationChanged extends AbstractDomainEvent {
  private final UserId userId;
  private final boolean isActive;

  public UserActivationChanged(UserId userId, boolean isActive, Instant occurredAt) {
    super(occurredAt);
    this.userId = requireNonNull(userId, "userId");
    this.isActive = isActive;
  }

  /**
   * Creates a new UserActivationChanged event.
   *
   * @param userId the ID of the user whose activation status changed
   * @param isActive the new activation status
   * @param occurredAt the timestamp when the event occurred
   * @return a new UserActivationChanged event
   */
  public static UserActivationChanged createEvent(UserId userId, boolean isActive, Instant occurredAt) {
    return new UserActivationChanged(userId, isActive, occurredAt);
  }

  /**
   * Creates a new UserActivationChanged event with current timestamp.
   *
   * @deprecated Use {@link #createEvent(UserId, boolean, Instant)} with explicit timestamp for deterministic behavior
   * @param userId the ID of the user whose activation status changed
   * @param isActive the new activation status
   * @return a new UserActivationChanged event
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public static UserActivationChanged createEvent(UserId userId, boolean isActive) {
    return createEvent(userId, isActive, Instant.now());
  }

  public UserId getUserId() {
    return userId;
  }

  public boolean isActive() {
    return isActive;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserActivationChanged that)) {
      return false;
    }

    return isActive == that.isActive && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, isActive);
  }

  @Override
  public String toString() {
    return "UserActivationChanged{"
        + "userId=" + userId
        + ", isActive=" + isActive
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
