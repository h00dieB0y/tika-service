package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionTest {
  @Nested
  @DisplayName("Valid permission scenarios")
  class ValidPermissionScenarios {

    @Test
    @DisplayName("Creates Permission object when permission name is valid")
    void createsPermissionWhenValid() {
      assertDoesNotThrow(() -> new Permission("read.user.data"));
    }

    @Test
    @DisplayName("Allows permission with maximum length of 100 characters")
    void allowsPermissionWithMaxLength() {
      // Create a valid permission format (action.resource.qualifier) that is exactly 100 characters
      String prefix = "read.";
      String resource = "user-";
      String suffix = ".data";
      int remainingChars = 100 - (prefix.length() + suffix.length());
      String validName = prefix + resource + "a".repeat(remainingChars - resource.length()) + suffix;

      assertDoesNotThrow(() -> new Permission(validName));
    }
  }

  @Nested
  @DisplayName("Invalid permission scenarios")
  class InvalidPermissionScenarios {

    @Test
    @DisplayName("Throws exception when permission is null")
    void throwsExceptionWhenPermissionIsNull() {
      assertThrows(InvalidPermissionException.class, () -> new Permission(null));
    }

    @Test
    @DisplayName("Throws exception when permission is blank")
    void throwsExceptionWhenPermissionIsBlank() {
      assertThrows(InvalidPermissionException.class, () -> new Permission("   "));
    }

    @Test
    @DisplayName("Throws exception when permission  exceeds 100 characters")
    void throwsExceptionWhenPermissionExceedsMaxLength() {
      String invalidName = "a".repeat(101);
      assertThrows(InvalidPermissionException.class, () -> new Permission(invalidName));
    }

    @Test
    @DisplayName("Throws exception when permission contains invalid characters")
    void throwsExceptionWhenPermissionContainsInvalidCharacters() {
      assertThrows(InvalidPermissionException.class, () -> new Permission("invalid_permission!"));
    }

    @Test
    @DisplayName("Throws exception when permission does not start with a lowercase letter")
    void throwsExceptionWhenPermissionDoesNotStartWithLowercase() {
      assertThrows(InvalidPermissionException.class, () -> new Permission("Invalid.Permission"));
    }

    @Test
    @DisplayName("Throws exception when permission ends with a dot")
    void throwsExceptionWhenPermissionEndsWithDot() {
      assertThrows(InvalidPermissionException.class, () -> new Permission("invalid.permission."));
    }

    @Test
    @DisplayName("Throws exception when permission has consecutive dots")
    void throwsExceptionWhenPermissionHasConsecutiveDots() {
      assertThrows(InvalidPermissionException.class, () -> new Permission("invalid..permission"));
    }
  }
}
