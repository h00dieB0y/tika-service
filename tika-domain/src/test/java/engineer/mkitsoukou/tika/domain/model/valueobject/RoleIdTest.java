package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RoleIdTest {

  @Nested
  @DisplayName("Valid role ID scenarios")
  class ValidRoleIdScenarios {

    @Test
    @DisplayName("Creates RoleId object when UUID is valid")
    void createsRoleIdWhenUuidIsValid() {
      UUID validUuid = UUID.randomUUID();
      RoleId roleId = RoleId.of(validUuid);

      assertNotNull(roleId);
      assertEquals(validUuid, roleId.value());
    }

    @Test
    @DisplayName("Creates RoleId object from valid UUID string")
    void createsRoleIdFromValidUuidString() {
      String validUuidString = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
      RoleId roleId = RoleId.of(validUuidString);

      assertNotNull(roleId);
      assertEquals(UUID.fromString(validUuidString), roleId.value());
    }

    @Test
    @DisplayName("Creates equal RoleId objects with same UUID value")
    void createsEqualRoleIdObjectsWithSameValue() {
      UUID uuid = UUID.randomUUID();
      RoleId roleId1 = RoleId.of(uuid);
      RoleId roleId2 = RoleId.of(uuid);

      assertEquals(roleId1, roleId2);
      assertEquals(roleId1.hashCode(), roleId2.hashCode());
    }
  }

  @Nested
  @DisplayName("Invalid role ID scenarios")
  class InvalidRoleIdScenarios {

    @Test
    @DisplayName("Throws exception when UUID is null")
    void throwsExceptionWhenUuidIsNull() {
      assertThrows(NullPointerException.class, () -> RoleId.of((UUID) null));
    }

    @Test
    @DisplayName("Throws exception when UUID string is null")
    void throwsExceptionWhenUuidStringIsNull() {
      assertThrows(NullPointerException.class, () -> RoleId.of((String) null));
    }

    @Test
    @DisplayName("Throws exception when UUID string is invalid")
    void throwsExceptionWhenUuidStringIsInvalid() {
      String invalidUuidString = "invalid-uuid-string";
      assertThrows(InvalidRoleIdException.class, () -> RoleId.of(invalidUuidString));
    }
  }
}
