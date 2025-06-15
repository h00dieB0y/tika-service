package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionNameTest {
  @Nested
  @DisplayName("Valid permission name scenarios")
  class ValidPermissionNameScenarios {

    @Test
    @DisplayName("Creates PermissionName object when name is valid")
    void createsPermissionNameWhenValid() {
      assertDoesNotThrow(() -> new PermissionName("read.user.data"));
    }

    @Test
    @DisplayName("Allows permission name with maximum length of 100 characters")
    void allowsPermissionNameWithMaxLength() {
      String validName = "a".repeat(99) + "b";
      assertDoesNotThrow(() -> new PermissionName(validName));
    }
  }

  @Nested
  @DisplayName("Invalid permission name scenarios")
  class InvalidPermissionNameScenarios {

    @Test
    @DisplayName("Throws exception when permission name is null")
    void throwsExceptionWhenPermissionNameIsNull() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName(null));
    }

    @Test
    @DisplayName("Throws exception when permission name is blank")
    void throwsExceptionWhenPermissionNameIsBlank() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName("   "));
    }

    @Test
    @DisplayName("Throws exception when permission name exceeds 100 characters")
    void throwsExceptionWhenPermissionNameExceedsMaxLength() {
      String invalidName = "a".repeat(101);
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName(invalidName));
    }

    @Test
    @DisplayName("Throws exception when permission name contains invalid characters")
    void throwsExceptionWhenPermissionNameContainsInvalidCharacters() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName("invalid_permission_name"));
    }

    @Test
    @DisplayName("Throws exception when permission name does not start with a lowercase letter")
    void throwsExceptionWhenPermissionNameDoesNotStartWithLowercase() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName("1invalid.name"));
    }

    @Test
    @DisplayName("Throws exception when permission name ends with a dot")
    void throwsExceptionWhenPermissionNameEndsWithDot() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName("invalid.name."));
    }

    @Test
    @DisplayName("Throws exception when permission name has consecutive dots")
    void throwsExceptionWhenPermissionNameHasConsecutiveDots() {
      assertThrows(InvalidPermissionNameException.class, () -> new PermissionName("invalid..name"));
    }
  }
}
