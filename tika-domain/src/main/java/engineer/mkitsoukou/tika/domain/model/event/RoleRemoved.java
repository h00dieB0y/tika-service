package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.Objects;

public class RoleRemoved extends AbstractDomainEvent {

  private final UserId userId;
  private final RoleId roleId;

  public RoleRemoved(UserId userId, RoleId roleId) {
    super();
    this.userId = requireNonNull(userId, "userId");
    this.roleId = requireNonNull(roleId, "roleId");
  }

  /**
   * Creates a new RoleRemoved event.
   *
   * @param userId the ID of the user from which the role was removed
   * @param roleId the ID of the role that was removed
   * @return a new RoleRemoved event
   */
  public static RoleRemoved createEvent(UserId userId, RoleId roleId) {
    return new RoleRemoved(userId, roleId);
  }

  public UserId getUserId() {
    return userId;
  }

  public RoleId getRoleId() {
    return roleId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RoleRemoved that)) {
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
    return "RoleRemoved{"
        + "roleId=" + roleId
        + ", userId=" + userId
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
