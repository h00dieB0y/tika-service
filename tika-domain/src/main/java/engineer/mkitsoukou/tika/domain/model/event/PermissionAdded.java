package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.time.Instant;
import java.util.Objects;

public class PermissionAdded implements DomainEvent {

  private final RoleId roleId;
  private final Permission permission;
  private final Instant occurredAt;

  public PermissionAdded(RoleId roleId, Permission permission) {
    this.roleId = Objects.requireNonNull(roleId, "roleId cannot be null");
    this.permission = Objects.requireNonNull(permission, "permission cannot be null");
    this.occurredAt = Instant.now();
  }

  @Override
  public Instant occurredAt() {
    return occurredAt;
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
        + ", occurredAt=" + occurredAt
        + '}';
  }

  public static PermissionAdded of(RoleId roleId, Permission permission) {
    return new PermissionAdded(roleId, permission);
  }
}
