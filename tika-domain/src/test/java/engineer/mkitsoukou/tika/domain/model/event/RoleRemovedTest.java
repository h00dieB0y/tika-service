package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleRemovedTest {

  @Test
  void createRoleRemovedEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());

    RoleRemoved event = new RoleRemoved(userId, roleId);

    assertEquals(userId, event.getUserId());
    assertEquals(roleId, event.getRoleId());
    assertNotNull(event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());

    RoleRemoved e1 = new RoleRemoved(userId, roleId);
    RoleRemoved e2 = RoleRemoved.of(userId, roleId);

    assertEquals(e1.getUserId(),   e2.getUserId());
    assertEquals(e1.getRoleId(),   e2.getRoleId());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    assertThrows(NullPointerException.class, () -> new RoleRemoved(null, roleId));
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    UserId userId = UserId.of(UUID.randomUUID());
    assertThrows(NullPointerException.class, () -> new RoleRemoved(userId, null));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());

    RoleRemoved e1 = new RoleRemoved(userId, roleId);
    RoleRemoved e2 = new RoleRemoved(userId, roleId);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    RoleRemoved e1 = new RoleRemoved(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()));
    RoleRemoved e2 = new RoleRemoved(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()));

    assertNotEquals(e1, e2);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    RoleRemoved event = new RoleRemoved(userId, roleId);

    String s = event.toString();
    assertTrue(s.contains("RoleRemoved"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains(roleId.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
