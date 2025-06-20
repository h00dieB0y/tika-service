package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.model.event.PasswordChanged;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PasswordChangedTest {

  @Test
  void createPasswordChangedEvent() {
    UserId userId = UserId.of(UUID.randomUUID());
    PasswordChanged event = new PasswordChanged(userId);

    assertEquals(userId, event.getUserId());
    assertNotNull(event.occurredAt());
  }

  @Test
  void factoryMethodCreatesEquivalentEvent() {
    UserId userId = UserId.of(UUID.randomUUID());

    PasswordChanged e1 = new PasswordChanged(userId);
    PasswordChanged e2 = PasswordChanged.createEvent(userId);

    assertEquals(e1.getUserId(), e2.getUserId());
  }

  @Test
  void throwsExceptionWhenUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> new PasswordChanged(null));
  }

  @Test
  void equalsAndHashCodeWork() {
    UserId userId = UserId.of(UUID.randomUUID());

    PasswordChanged e1 = new PasswordChanged(userId);
    PasswordChanged e2 = new PasswordChanged(userId);

    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  @Test
  void differentEventsAreNotEqual() {
    PasswordChanged e1 = new PasswordChanged(UserId.of(UUID.randomUUID()));
    PasswordChanged e2 = new PasswordChanged(UserId.of(UUID.randomUUID()));

    assertNotEquals(e1, e2);
  }

  @Test
  void toStringContainsRelevantInformation() {
    UserId userId = UserId.of(UUID.randomUUID());
    PasswordChanged event = new PasswordChanged(userId);

    String s = event.toString();
    assertTrue(s.contains("PasswordChanged"));
    assertTrue(s.contains(userId.toString()));
    assertTrue(s.contains("occurredAt"));
  }
}
