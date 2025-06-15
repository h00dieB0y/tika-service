package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.model.event.DomainEvent;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;
import java.util.*;

public final class Role {
  private final transient List<DomainEvent> events = new ArrayList<>();
  private final RoleId roleId;
  private final RoleName roleName;
  private final Set<Permission> permissions;

  public Role(RoleId roleId, RoleName roleName, Set<Permission> permissions) {
    if (roleId == null) throw new IllegalArgumentException("roleId cannot be null");
    if (roleName == null) throw new IllegalArgumentException("roleName cannot be null");
    if (permissions == null) throw new IllegalArgumentException("permissions cannot be null");
    this.roleId = roleId;
    this.roleName = roleName;
    this.permissions = new HashSet<>(permissions);
  }

  public RoleId getRoleId() {
    return roleId;
  }

  public RoleName getRoleName() {
    return roleName;
  }

  public Set<Permission> getPermissions() {
    return Collections.unmodifiableSet(permissions);
  }

  public void addPermission(Permission permission) {
    if (permission == null) throw new IllegalArgumentException("permission cannot be null");
    if (permissions.add(permission)) {
      events.add(new PermissionAdded(roleId, permission));
    }
  }

  public void removePermission(Permission permission) {
    if (permission == null) throw new IllegalArgumentException("permission cannot be null");
    if (permissions.remove(permission)) {
      events.add(new PermissionRemoved(roleId, permission));
    }
  }

  public List<DomainEvent> pullEvents() {
    List<DomainEvent> pulledEvents = new ArrayList<>(events);
    events.clear();
    return pulledEvents;
  }
}
