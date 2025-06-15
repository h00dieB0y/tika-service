package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.time.Instant;
import java.util.Objects;

public class PermissionRemoved implements DomainEvent {

  private final RoleId roleId;
  private final Permission permission;
  private final Instant occurredAt;

  public PermissionRemoved(RoleId roleId, Permission permission) {
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
    PermissionRemoved that = (PermissionRemoved) o;
    return Objects.equals(roleId, that.roleId)
        && Objects.equals(permission, that.permission)
        && Objects.equals(occurredAt, that.occurredAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, permission, occurredAt);
  }

  @Override
  public String toString() {
    return "PermissionRemoved{"
        + "roleId=" + roleId
        + ", permission=" + permission
        + ", occurredAt=" + occurredAt
        + '}';
  }

  public static PermissionRemoved of(RoleId roleId, Permission permission) {
    return new PermissionRemoved(roleId, permission);
  }
}
