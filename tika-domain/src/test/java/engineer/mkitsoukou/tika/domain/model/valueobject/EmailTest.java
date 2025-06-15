package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

  @Nested
  @DisplayName("Valid email scenarios")
  class ValidEmailScenarios {

    @Test
    @DisplayName("Creates Email object when email is valid")
    void createsEmailWhenValid() {
      assertDoesNotThrow(() -> new Email("valid.email@example.com"));
    }

    @Test
    @DisplayName("Trims whitespace around valid email")
    void trimsWhitespaceAroundValidEmail() {
      assertDoesNotThrow(() -> new Email("   valid.email@example.com   "));
    }
  }

  @Nested
  @DisplayName("Invalid email scenarios")
  class InvalidEmailScenarios {

    @Test
    @DisplayName("Throws exception when email is null")
    void throwsExceptionWhenEmailIsNull() {
      assertThrows(InvalidEmailException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("Throws exception when email is blank")
    void throwsExceptionWhenEmailIsBlank() {
      assertThrows(InvalidEmailException.class, () -> new Email("   "));
    }

    @Test
    @DisplayName("Throws exception when email is invalid")
    void throwsExceptionWhenEmailIsInvalid() {
      assertThrows(InvalidEmailException.class, () -> new Email("invalid-email"));
    }
  }
}
