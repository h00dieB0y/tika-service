package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionNameException;
import engineer.mkitsoukou.tika.domain.exception.InvalidRoleIdException;
import engineer.mkitsoukou.tika.domain.exception.InvalidRoleNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoleNameTest {
  @Nested
  @DisplayName("Valid role name scenarios")
  class ValidRoleNameScenarios {

    @Test
    @DisplayName("Creates RoleName object when name is valid")
    void createsRoleNameWhenValid() {
      assertDoesNotThrow(() -> new RoleName("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Allows role name without ROLE_ prefix")
    void allowsRoleNameWithoutRolePrefix() {
      assertDoesNotThrow(() -> new RoleName("ADMIN"));
    }

    @Test
    @DisplayName("Allows role name with maximum length of 100 characters")
    void allowsRoleNameWithMaxLength() {
      String validName = "ROLE_" + "A".repeat(94);
      assertDoesNotThrow(() -> new RoleName(validName));
    }
  }

  @Nested
  @DisplayName("Invalid role name scenarios")
  class InvalidRoleNameScenarios {

    @Test
    @DisplayName("Throws exception when role name is null")
    void throwsExceptionWhenRoleNameIsNull() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName(null));
    }

    @Test
    @DisplayName("Throws exception when role name is blank")
    void throwsExceptionWhenRoleNameIsBlank() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName("   "));
    }

    @Test
    @DisplayName("Throws exception when role name exceeds 100 characters")
    void throwsExceptionWhenRoleNameExceedsMaxLength() {
      String invalidName = "ROLE_" + "A".repeat(96);
      assertThrows(InvalidRoleNameException.class, () -> new RoleName(invalidName));
    }

    @Test
    @DisplayName("Throws exception when role name contains invalid characters")
    void throwsExceptionWhenRoleNameContainsInvalidCharacters() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName("ROLE_ADMIN!"));
    }

    @Test
    @DisplayName("Throws exception when role name does not start with an uppercase letter")
    void throwsExceptionWhenRoleNameDoesNotStartWithUppercase() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName("role_admin"));
    }

    @Test
    @DisplayName("Throws exception when role name has consecutive underscores")
    void throwsExceptionWhenRoleNameHasConsecutiveUnderscores() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName("ROLE__ADMIN"));
    }

    @Test
    @DisplayName("Throws exception when role name ends with an underscore")
    void throwsExceptionWhenRoleNameEndsWithUnderscore() {
      assertThrows(InvalidRoleNameException.class, () -> new RoleName("ROLE_ADMIN_"));
    }
  }
}
