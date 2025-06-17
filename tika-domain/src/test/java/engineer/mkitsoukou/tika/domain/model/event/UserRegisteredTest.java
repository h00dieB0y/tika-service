package engineer.mkitsoukou.tika.domain.model.event;

import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisteredTest {

  @Test
  void createUserRegisteredEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("alice@example.com");

    UserRegistered event = new UserRegistered(userId, email);

    assertEquals(userId, event.getUserId());
    assertEquals(email,  event.getEmail());
    assertNotNull(event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("bob@example.com");

    UserRegistered evt1 = new UserRegistered(userId, email);
    UserRegistered evt2 = UserRegistered.of(userId, email);

    assertEquals(evt1.getUserId(), evt2.getUserId());
    assertEquals(evt1.getEmail(),  evt2.getEmail());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    Email email = new Email("charlie@example.com");
    assertThrows(NullPointerException.class, () -> new UserRegistered(null, email));
  }

  @Test
  void throwsExceptionWhenEmailIsNull() {
    UserId userId = UserId.of(UUID.randomUUID());
    assertThrows(NullPointerException.class, () -> new UserRegistered(userId, null));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("dana@example.com");

    UserRegistered e1 = new UserRegistered(userId, email);
    UserRegistered e2 = new UserRegistered(userId, email);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    UserId id1 = UserId.of(UUID.randomUUID());
    UserId id2 = UserId.of(UUID.randomUUID());
    Email e1 = new Email("e1@example.com");
    Email e2 = new Email("e2@example.com");

    UserRegistered ev1 = new UserRegistered(id1, e1);
    UserRegistered ev2 = new UserRegistered(id2, e1);
    UserRegistered ev3 = new UserRegistered(id1, e2);

    assertNotEquals(ev1, ev2);
    assertNotEquals(ev1, ev3);
    assertNotEquals(ev2, ev3);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    Email email = new Email("frank@example.com");
    UserRegistered event = new UserRegistered(userId, email);

    String s = event.toString();
    assertTrue(s.contains("UserRegistered"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains(email.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
