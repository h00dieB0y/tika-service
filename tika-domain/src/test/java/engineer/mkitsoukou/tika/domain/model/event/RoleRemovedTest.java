package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleRemovedTest {

  @Test
  void createRoleRemovedEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleRemoved event = new RoleRemoved(userId, roleId, now);

    assertEquals(userId, event.getUserId());
    assertEquals(roleId, event.getRoleId());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleRemoved e1 = new RoleRemoved(userId, roleId, now);
    RoleRemoved e2 = RoleRemoved.createEvent(userId, roleId, now);

    assertEquals(e1.getUserId(),   e2.getUserId());
    assertEquals(e1.getRoleId(),   e2.getRoleId());
    assertEquals(e1.occurredAt(),  e2.occurredAt());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new RoleRemoved(null, roleId, now));
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new RoleRemoved(userId, null, now));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleRemoved e1 = new RoleRemoved(userId, roleId, now);
    RoleRemoved e2 = new RoleRemoved(userId, roleId, now);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    Instant now = Instant.now();
    RoleRemoved e1 = new RoleRemoved(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()), now);
    RoleRemoved e2 = new RoleRemoved(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()), now);

    assertNotEquals(e1, e2);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();
    RoleRemoved event = new RoleRemoved(userId, roleId, now);

    String s = event.toString();
    assertTrue(s.contains("RoleRemoved"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains(roleId.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
