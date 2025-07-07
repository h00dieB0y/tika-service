package engineer.mkitsoukou.tika.domain.model.event;

import static org.junit.jupiter.api.Assertions.*;

import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PermissionAddedTest {

  @Test
  void createPermissionAddedEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");
    Instant now = Instant.now();

    PermissionAdded event = new PermissionAdded(roleId, permission, now);

    assertEquals(roleId, event.getRoleId());
    assertEquals(permission, event.getPermission());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("write.users");
    Instant now = Instant.now();

    PermissionAdded event1 = new PermissionAdded(roleId, permission, now);
    PermissionAdded event2 = PermissionAdded.createEvent(roleId, permission, now);

    assertEquals(event1.getRoleId(), event2.getRoleId());
    assertEquals(event1.getPermission(), event2.getPermission());
    assertEquals(event1.occurredAt(), event2.occurredAt());
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    Permission permission = new Permission("read.users");
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () -> {
      new PermissionAdded(null, permission, now);
    });
  }

  @Test
  void throwsExceptionWhenPermissionIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    assertThrows(NullPointerException.class, () -> {
      new PermissionAdded(roleId, null, now);
    });
  }

  @Test
  void equalsAndHashCodeWork() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");
    Instant now = Instant.now();

    PermissionAdded event1 = new PermissionAdded(roleId, permission, now);
    PermissionAdded event2 = new PermissionAdded(roleId, permission, now);

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    RoleId roleId1 = RoleId.of(UUID.randomUUID());
    RoleId roleId2 = RoleId.of(UUID.randomUUID());
    Permission permission1 = new Permission("read.users");
    Permission permission2 = new Permission("write.user");
    Instant now = Instant.now();

    PermissionAdded event1 = new PermissionAdded(roleId1, permission1, now);
    PermissionAdded event2 = new PermissionAdded(roleId2, permission1, now);
    PermissionAdded event3 = new PermissionAdded(roleId1, permission2, now);

    assertNotEquals(event1, event2);
    assertNotEquals(event1, event3);
    assertNotEquals(event2, event3);
  }

  @Test
  void toStringContainsRelevantInformation() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Permission permission = new Permission("read.users");
    Instant now = Instant.now();

    PermissionAdded event = new PermissionAdded(roleId, permission, now);
    String toString = event.toString();

    assertTrue(toString.contains(roleId.toString()));
    assertTrue(toString.contains(permission.toString()));
    assertTrue(toString.contains("occurredAt"));
  }
}
