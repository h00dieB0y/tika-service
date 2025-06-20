package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Represents a role with a set of permissions in the system.
 * Roles can be assigned to users to grant them access to specific functionalities.
 */
public final class Role extends AbstractEntity {
  private final RoleId roleId;
  private final RoleName roleName;
  private final Set<Permission> permissions;

  /**
   * Creates a new role with the specified ID, name, and permissions.
   *
   * @param roleId      the unique identifier of the role
   * @param roleName    the name of the role
   * @param permissions the initial set of permissions for this role
   * @throws EntityRequiredFieldException if any parameter is null
   */
  private Role(RoleId roleId, RoleName roleName, Set<Permission> permissions) {
    this.roleId = requireNonNull(roleId, "roleId");
    this.roleName = requireNonNull(roleName, "roleName");
    this.permissions = new HashSet<>(requireNonNull(permissions, "permissions"));
  }

  /**
   * Factory method to create a new role with a generated ID.
   *
   * @param roleName    the name of the role
   * @param permissions the initial set of permissions for this role
   * @return a new Role instance
   * @throws EntityRequiredFieldException if any parameter is null
   */
  public static Role of(RoleName roleName, Set<Permission> permissions) {
    return new Role(RoleId.generate(), roleName, permissions);
  }

  /**
   * Gets the unique identifier of this role.
   *
   * @return the role ID
   */
  public RoleId getRoleId() {
    return roleId;
  }

  /**
   * Gets the name of this role.
   *
   * @return the role name
   */
  public RoleName getRoleName() {
    return roleName;
  }

  /**
   * Gets an unmodifiable view of the permissions assigned to this role.
   *
   * @return the set of permissions
   */
  public Set<Permission> getPermissions() {
    return Collections.unmodifiableSet(permissions);
  }

  /**
   * Adds a permission to this role.
   * If the permission is already assigned, no action is taken.
   *
   * @param permission the permission to add
   * @throws EntityRequiredFieldException if the permission is null
   */
  public void addPermission(Permission permission) {
    requireNonNull(permission, "permission");

    if (permissions.add(permission)) {
      recordEvent(PermissionAdded.of(roleId, permission));
    }
  }

  /**
   * Removes a permission from this role.
   * If the permission is not assigned, no action is taken.
   *
   * @param permission the permission to remove
   * @throws EntityRequiredFieldException if the permission is null
   */
  public void removePermission(Permission permission) {
    requireNonNull(permission, "permission");

    if (permissions.remove(permission)) {
      recordEvent(PermissionRemoved.of(roleId, permission));
    }
  }

  /**
   * Checks if this role has a specific permission.
   *
   * @param permission the permission to check
   * @return true if the role has the permission, false otherwise
   * @throws EntityRequiredFieldException if the permission is null
   */
  public boolean hasPermission(Permission permission) {
    requireNonNull(permission, "permission");
    return permissions.contains(permission);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Role role)) return false;
    return Objects.equals(roleId, role.roleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId);
  }

  @Override
  public String toString() {
    return "Role{" +
      "roleId=" + roleId +
      ", roleName=" + roleName +
      ", permissionsCount=" + permissions.size() +
      '}';
  }
}
