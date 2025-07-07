package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PermissionRemovedTest {
  @Test
  void createPermissionRemovedEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");
    Instant now = Instant.now();

    PermissionRemoved event = new PermissionRemoved(roleId, permission, now);

    assertEquals(roleId, event.getRoleId());
    assertEquals(permission, event.getPermission());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("update.users");
    Instant now = Instant.now();

    PermissionRemoved event1 = new PermissionRemoved(roleId, permission, now);
    PermissionRemoved event2 = PermissionRemoved.createEvent(roleId, permission, now);

    assertEquals(event1.getRoleId(), event2.getRoleId());
    assertEquals(event1.getPermission(), event2.getPermission());
    assertEquals(event1.occurredAt(), event2.occurredAt());
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    Permission permission = new Permission("delete.users");
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () -> {
      new PermissionRemoved(null, permission, now);
    });
  }

  @Test
  void throwsExceptionWhenPermissionIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () -> {
      new PermissionRemoved(roleId, null, now);
    });
  }

  @Test
  void equalsAndHashCodeWorkForPermissionRemoved() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");
    Instant now = Instant.now();

    PermissionRemoved event1 = new PermissionRemoved(roleId, permission, now);
    PermissionRemoved event2 = new PermissionRemoved(roleId, permission, now);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void differentPermissionRemovedEventsAreNotEqual() {
    RoleId roleId1 = RoleId.of(UUID.randomUUID());
    RoleId roleId2 = RoleId.of(UUID.randomUUID());
    Permission permission1 = new Permission("delete.users");
    Permission permission2 = new Permission("update.users");
    Instant now = Instant.now();

    PermissionRemoved event1 = new PermissionRemoved(roleId1, permission1, now);
    PermissionRemoved event2 = new PermissionRemoved(roleId2, permission1, now);
    PermissionRemoved event3 = new PermissionRemoved(roleId1, permission2, now);

    assertNotEquals(event1, event2);
    assertNotEquals(event1, event3);
    assertNotEquals(event2, event3);
  }

  @Test
  void toStringContainsRelevantInformationForPermissionRemoved() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("delete.users");
    Instant now = Instant.now();

    PermissionRemoved event = new PermissionRemoved(roleId, permission, now);
    String toString = event.toString();

    assertTrue(toString.contains(roleId.toString()));
    assertTrue(toString.contains(permission.toString()));
    assertTrue(toString.contains("occurredAt"));
  }
}
