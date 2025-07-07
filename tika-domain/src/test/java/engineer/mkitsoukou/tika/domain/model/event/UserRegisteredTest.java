package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisteredTest {

  @Test
  void createUserRegisteredEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("alice@example.com");
    Instant now = Instant.now();

    UserRegistered event = new UserRegistered(userId, email, now);

    assertEquals(userId, event.getUserId());
    assertEquals(email,  event.getEmail());
    assertEquals(now, event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("bob@example.com");
    Instant now = Instant.now();

    UserRegistered evt1 = new UserRegistered(userId, email, now);
    UserRegistered evt2 = UserRegistered.createEvent(userId, email, now);

    assertEquals(evt1.getUserId(), evt2.getUserId());
    assertEquals(evt1.getEmail(),  evt2.getEmail());
    assertEquals(evt1.occurredAt(), evt2.occurredAt());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    Email email = new Email("charlie@example.com");
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new UserRegistered(null, email, now));
  }

  @Test
  void throwsExceptionWhenEmailIsNull() {
    UserId userId = UserId.of(UUID.randomUUID());
    Instant now = Instant.now();
    assertThrows(NullPointerException.class, () -> new UserRegistered(userId, null, now));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("dana@example.com");
    Instant now = Instant.now();

    UserRegistered e1 = new UserRegistered(userId, email, now);
    UserRegistered e2 = new UserRegistered(userId, email, now);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    UserId id1 = UserId.of(UUID.randomUUID());
    UserId id2 = UserId.of(UUID.randomUUID());
    Email e1 = new Email("e1@example.com");
    Email e2 = new Email("e2@example.com");
    Instant now = Instant.now();

    UserRegistered ev1 = new UserRegistered(id1, e1, now);
    UserRegistered ev2 = new UserRegistered(id2, e1, now);
    UserRegistered ev3 = new UserRegistered(id1, e2, now);

    assertNotEquals(ev1, ev2);
    assertNotEquals(ev1, ev3);
    assertNotEquals(ev2, ev3);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("frank@example.com");
    Instant now = Instant.now();
    UserRegistered event = new UserRegistered(userId, email, now);

    String s = event.toString();
    assertTrue(s.contains("UserRegistered"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains(email.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
