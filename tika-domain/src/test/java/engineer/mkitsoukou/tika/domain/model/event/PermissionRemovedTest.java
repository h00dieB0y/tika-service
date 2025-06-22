package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionRemovedTest {
  @Test
  void createPermissionRemovedEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");

    PermissionRemoved event = new PermissionRemoved(roleId, permission);

    assertEquals(roleId, event.getRoleId());
    assertEquals(permission, event.getPermission());
    assertNotNull(event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("update.users");

    PermissionRemoved event1 = new PermissionRemoved(roleId, permission);
    PermissionRemoved event2 = PermissionRemoved.createEvent(roleId, permission);

    assertEquals(event1.getRoleId(), event2.getRoleId());
    assertEquals(event1.getPermission(), event2.getPermission());
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    Permission permission = new Permission("delete.users");

    assertThrows(NullPointerException.class, () -> {
      new PermissionRemoved(null, permission);
    });
  }

  @Test
  void throwsExceptionWhenPermissionIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());

    assertThrows(NullPointerException.class, () -> {
      new PermissionRemoved(roleId, null);
    });
  }

  @Test
  void equalsAndHashCodeWorkForPermissionRemoved() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");

    PermissionRemoved event1 = new PermissionRemoved(roleId, permission);
    PermissionRemoved event2 = new PermissionRemoved(roleId, permission);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void differentPermissionRemovedEventsAreNotEqual() {
    RoleId roleId1 = RoleId.of(UUID.randomUUID());
    RoleId roleId2 = RoleId.of(UUID.randomUUID());
    Permission permission1 = new Permission("delete.users");
    Permission permission2 = new Permission("update.users");

    PermissionRemoved event1 = new PermissionRemoved(roleId1, permission1);
    PermissionRemoved event2 = new PermissionRemoved(roleId2, permission1);
    PermissionRemoved event3 = new PermissionRemoved(roleId1, permission2);

    assertNotEquals(event1, event2);
    assertNotEquals(event1, event3);
    assertNotEquals(event2, event3);
  }

  @Test
  void toStringContainsRelevantInformationForPermissionRemoved() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");

    PermissionRemoved event = new PermissionRemoved(roleId, permission);
    String toString = event.toString();

    assertTrue(toString.contains(roleId.toString()));
    assertTrue(toString.contains(permission.toString()));
    assertTrue(toString.contains("occurredAt"));
  }
}
