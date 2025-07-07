package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.time.Instant;
import java.util.Objects;

public class PermissionRemoved extends AbstractDomainEvent {

  private final RoleId roleId;
  private final Permission permission;

  public PermissionRemoved(RoleId roleId, Permission permission, Instant occurredAt) {
    super(occurredAt);
    this.roleId = requireNonNull(roleId, "roleId");
    this.permission = requireNonNull(permission, "permission");
  }

  /**
   * Creates a new PermissionRemoved event.
   *
   * @param roleId the ID of the role from which the permission was removed
   * @param permission the permission that was removed
   * @param occurredAt the timestamp when the event occurred
   * @return a new PermissionRemoved event
   */
  public static PermissionRemoved createEvent(RoleId roleId, Permission permission, Instant occurredAt) {
    return new PermissionRemoved(roleId, permission, occurredAt);
  }

  /**
   * Creates a new PermissionRemoved event with current timestamp.
   *
   * @deprecated Use {@link #createEvent(RoleId, Permission, Instant)} with explicit timestamp for deterministic behavior
   * @param roleId the ID of the role from which the permission was removed
   * @param permission the permission that was removed
   * @return a new PermissionRemoved event
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public static PermissionRemoved createEvent(RoleId roleId, Permission permission) {
    return createEvent(roleId, permission, Instant.now());
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
        && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, permission);
  }

  @Override
  public String toString() {
    return "PermissionRemoved{"
        + "roleId=" + roleId
        + ", permission=" + permission
        + ", occurredAt=" + occurredAt()
        + '}';
  }
}
