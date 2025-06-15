package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlainPasswordTest {
  @Nested
  @DisplayName("Valid password scenarios")
  class ValidPasswordScenarios {

    @Test
    @DisplayName("Creates PlainPassword object when password meets all requirements")
    void createsPlainPasswordWhenValid() {
      assertDoesNotThrow(() -> new PlainPassword("Valid1@Password"));
    }

    @Test
    @DisplayName("Trims whitespace around valid password")
    void trimsWhitespaceAroundValidPassword() {
      assertDoesNotThrow(() -> new PlainPassword("   Valid1@Password   "));
    }
  }

  @Nested
  @DisplayName("Invalid password scenarios")
  class InvalidPasswordScenarios {

    @Test
    @DisplayName("Throws exception when password is null")
    void throwsExceptionWhenPasswordIsNull() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword(null));
    }

    @Test
    @DisplayName("Throws exception when password is blank")
    void throwsExceptionWhenPasswordIsBlank() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("   "));
    }

    @Test
    @DisplayName("Throws exception when password is too short")
    void throwsExceptionWhenPasswordIsTooShort() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("Short1@"));
    }

    @Test
    @DisplayName("Throws exception when password is too long")
    void throwsExceptionWhenPasswordIsTooLong() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("A".repeat(65) + "1@"));
    }

    @Test
    @DisplayName("Throws exception when password lacks an uppercase letter")
    void throwsExceptionWhenPasswordLacksUppercase() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("valid1@password"));
    }

    @Test
    @DisplayName("Throws exception when password lacks a lowercase letter")
    void throwsExceptionWhenPasswordLacksLowercase() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("VALID1@PASSWORD"));
    }

    @Test
    @DisplayName("Throws exception when password lacks a digit")
    void throwsExceptionWhenPasswordLacksDigit() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("Valid@Password"));
    }

    @Test
    @DisplayName("Throws exception when password lacks a special character")
    void throwsExceptionWhenPasswordLacksSpecialCharacter() {
      assertThrows(InvalidPasswordException.class, () -> new PlainPassword("Valid1Password"));
    }
  }
}
