package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoleAssignedTest {

  @Test
  void createRoleAssignedEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleAssigned event = new RoleAssigned(userId, roleId, now);

    assertEquals(userId, event.getUserId());
    assertEquals(roleId, event.getRoleId());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleAssigned e1 = new RoleAssigned(userId, roleId, now);
    RoleAssigned e2 = RoleAssigned.createEvent(userId, roleId, now);

    assertEquals(e1.getUserId(),   e2.getUserId());
    assertEquals(e1.getRoleId(),   e2.getRoleId());
    assertEquals(e1.occurredAt(),  e2.occurredAt());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new RoleAssigned(null, roleId, now));
  }

  @Test
  void throwsExceptionWhenRoleIdIsNull() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new RoleAssigned(userId, null, now));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();

    RoleAssigned e1 = new RoleAssigned(userId, roleId, now);
    RoleAssigned e2 = new RoleAssigned(userId, roleId, now);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    Instant now = Instant.now();
    RoleAssigned e1 = new RoleAssigned(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()), now);
    RoleAssigned e2 = new RoleAssigned(UserId.of(UUID.randomUUID()), RoleId.of(UUID.randomUUID()), now);

    assertNotEquals(e1, e2);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    RoleId roleId = RoleId.of(UUID.randomUUID());
    Instant now = Instant.now();
    RoleAssigned event = new RoleAssigned(userId, roleId, now);

    String s = event.toString();
    assertTrue(s.contains("RoleAssigned"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains(roleId.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
