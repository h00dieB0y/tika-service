package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class RoleAssigned extends AbstractDomainEvent {
  private final UserId userId;
  private final RoleId roleId;

  public RoleAssigned(UserId userId, RoleId roleId, Instant occurredAt) {
    super(occurredAt);
    this.userId = requireNonNull(userId, "userId");
    this.roleId = requireNonNull(roleId, "roleId");
  }

  /**
   * Creates a new RoleAssigned event.
   *
   * @param userId the ID of the user to which the role was assigned
   * @param roleId the ID of the role that was assigned
   * @param occurredAt the timestamp when the event occurred
   * @return a new RoleAssigned event
   */
  public static RoleAssigned createEvent(UserId userId, RoleId roleId, Instant occurredAt) {
    return new RoleAssigned(userId, roleId, occurredAt);
  }

  /**
   * Creates a new RoleAssigned event with current timestamp.
   *
   * @deprecated Use {@link #createEvent(UserId, RoleId, Instant)} with explicit timestamp for deterministic behavior
   * @param userId the ID of the user to which the role was assigned
   * @param roleId the ID of the role that was assigned
   * @return a new RoleAssigned event
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public static RoleAssigned createEvent(UserId userId, RoleId roleId) {
    return createEvent(userId, roleId, Instant.now());
  }

  public RoleId getRoleId() {
    return roleId;
  }

  public UserId getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleAssigned that)) {
      return false;
    }

    return Objects.equals(roleId, that.roleId)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, userId);
  }

  @Override
  public String toString() {
    return "RoleAssigned{"
        + "roleId=" + roleId
        + ", userId=" + userId
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
