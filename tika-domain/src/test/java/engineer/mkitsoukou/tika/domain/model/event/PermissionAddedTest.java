package engineer.mkitsoukou.tika.domain.model.event;

import static org.junit.jupiter.api.Assertions.*;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PermissionAddedTest {

  @Test
  void createPermissionAddedEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");

    PermissionAdded event = new PermissionAdded(roleId, permission);

    assertEquals(roleId, event.getRoleId());
    assertEquals(permission, event.getPermission());
    assertNotNull(event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("write.users");

    PermissionAdded event1 = new PermissionAdded(roleId, permission);
    PermissionAdded event2 = PermissionAdded.createEvent(roleId, permission);

    assertEquals(event1.getRoleId(), event2.getRoleId());
    assertEquals(event1.getPermission(), event2.getPermission());
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    Permission permission = new Permission("read.users");

    assertThrows(NullPointerException.class, () -> {
      new PermissionAdded(null, permission);
    });
  }

  @Test
  void throwsExceptionWhenPermissionIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());

    assertThrows(NullPointerException.class, () -> {
      new PermissionAdded(roleId, null);
    });
  }

  @Test
  void equalsAndHashCodeWork() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");

    PermissionAdded event1 = new PermissionAdded(roleId, permission);
    PermissionAdded event2 = new PermissionAdded(roleId, permission);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    RoleId roleId1 = RoleId.of(UUID.randomUUID());
    RoleId roleId2 = RoleId.of(UUID.randomUUID());
    Permission permission1 = new Permission("read.users");
    Permission permission2 = new Permission("write.user");

    PermissionAdded event1 = new PermissionAdded(roleId1, permission1);
    PermissionAdded event2 = new PermissionAdded(roleId2, permission1);
    PermissionAdded event3 = new PermissionAdded(roleId1, permission2);

    assertNotEquals(event1, event2);
    assertNotEquals(event1, event3);
    assertNotEquals(event2, event3);
  }

  @Test
  void toStringContainsRelevantInformation() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");

    PermissionAdded event = new PermissionAdded(roleId, permission);
    String toString = event.toString();

    assertTrue(toString.contains(roleId.toString()));
    assertTrue(toString.contains(permission.toString()));
    assertTrue(toString.contains("occurredAt"));
  }
}
