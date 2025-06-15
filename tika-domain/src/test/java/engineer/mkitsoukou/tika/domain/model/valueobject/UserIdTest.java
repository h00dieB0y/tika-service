package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidUserIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserIdTest {

  @Nested
  @DisplayName("Valid user ID scenarios")
  class ValidUserIdScenarios {

    @Test
    @DisplayName("Creates UserId object when UUID is valid")
    void createsUserIdWhenUuidIsValid() {
      UUID validUuid = UUID.randomUUID();
      UserId userId = UserId.of(validUuid);

      assertNotNull(userId);
      assertEquals(validUuid, userId.value());
    }

    @Test
    @DisplayName("Creates UserId object from valid UUID string")
    void createsUserIdFromValidUuidString() {
      String validUuidString = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
      UserId userId = UserId.of(validUuidString);

      assertNotNull(userId);
      assertEquals(UUID.fromString(validUuidString), userId.value());
    }

    @Test
    @DisplayName("Creates equal UserId objects with same UUID value")
    void createsEqualUserIdObjectsWithSameValue() {
      UUID uuid = UUID.randomUUID();
      UserId userId1 = UserId.of(uuid);
      UserId userId2 = UserId.of(uuid);

      assertEquals(userId1, userId2);
      assertEquals(userId1.hashCode(), userId2.hashCode());
    }
  }

  @Nested
  @DisplayName("Invalid user ID scenarios")
  class InvalidUserIdScenarios {

    @Test
    @DisplayName("Throws exception when UUID is null")
    void throwsExceptionWhenUuidIsNull() {
      assertThrows(NullPointerException.class, () -> UserId.of((UUID) null));
    }

    @Test
    @DisplayName("Throws exception when UUID string is null")
    void throwsExceptionWhenUuidStringIsNull() {
      assertThrows(NullPointerException.class, () -> UserId.of((String) null));
    }

    @Test
    @DisplayName("Throws exception when UUID string is invalid")
    void throwsExceptionWhenUuidStringIsInvalid() {
      String invalidUuidString = "invalid-uuid-string";
      assertThrows(InvalidUserIdException.class, () -> UserId.of(invalidUuidString));
    }
  }
}
