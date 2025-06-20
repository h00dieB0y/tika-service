package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.Objects;

public class RoleAssigned extends AbstractDomainEvent {
  private final UserId userId;
  private final RoleId roleId;

  public RoleAssigned(UserId userId, RoleId roleId) {
    super();
    this.userId = requireNonNull(userId, "userId");
    this.roleId = requireNonNull(roleId, "roleId");
  }

  /**
   * Creates a new RoleAssigned event.
   *
   * @param userId the ID of the user to which the role was assigned
   * @param roleId the ID of the role that was assigned
   * @return a new RoleAssigned event
   */
  public static RoleAssigned of(UserId userId, RoleId roleId) {
    return new RoleAssigned(userId, roleId);
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
