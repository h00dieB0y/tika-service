package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.util.Objects;

public class PermissionAdded extends AbstractDomainEvent {

  private final RoleId roleId;
  private final Permission permission;

  public PermissionAdded(RoleId roleId, Permission permission) {
    super();
    this.roleId = requireNonNull(roleId, "roleId");
    this.permission = requireNonNull(permission, "permission");
  }

  /**
   * Creates a new PermissionAdded event.
   *
   * @param roleId the ID of the role to which the permission was added
   * @param permission the permission that was added
   * @return a new PermissionAdded event
   */
  public static PermissionAdded createEvent(RoleId roleId, Permission permission) {
    return new PermissionAdded(roleId, permission);
  }

  public RoleId getRoleId() {
    return roleId;
  }

  public Permission getPermission() {
    return permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PermissionAdded that = (PermissionAdded) o;
    return Objects.equals(roleId, that.roleId)
        && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, permission);
  }

  @Override
  public String toString() {
    return "PermissionAdded{"
        + "roleId=" + roleId
        + ", permission=" + permission
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
