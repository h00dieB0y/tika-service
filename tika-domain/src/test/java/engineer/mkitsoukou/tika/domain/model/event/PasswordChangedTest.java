package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordChangedTest {

  @Test
  void createPasswordChangedEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();
    PasswordChanged event = new PasswordChanged(userId, now);

    assertEquals(userId, event.getUserId());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();

    PasswordChanged e1 = new PasswordChanged(userId, now);
    PasswordChanged e2 = PasswordChanged.createEvent(userId, now);

    assertEquals(e1.getUserId(), e2.getUserId());
    assertEquals(e1.occurredAt(), e2.occurredAt());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new PasswordChanged(null, now));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();

    PasswordChanged e1 = new PasswordChanged(userId, now);
    PasswordChanged e2 = new PasswordChanged(userId, now);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    Instant now = Instant.now();
    PasswordChanged e1 = new PasswordChanged(UserId.of(UUID.randomUUID()), now);
    PasswordChanged e2 = new PasswordChanged(UserId.of(UUID.randomUUID()), now);

    assertNotEquals(e1, e2);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();
    PasswordChanged event = new PasswordChanged(userId, now);

    String s = event.toString();
    assertTrue(s.contains("PasswordChanged"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
