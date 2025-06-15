package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasswordHashTest {
  @Nested
  @DisplayName("Valid hash scenarios")
  class ValidHashScenarios {

    @Test
    @DisplayName("Creates PasswordHash object when hash is valid")
    void createsPasswordHashWhenValid() {
      assertDoesNotThrow(() -> new PasswordHash("$2a$10$EixZaYVK1fsbw1Zfbx3OpO"));
    }
  }

  @Nested
  @DisplayName("Invalid hash scenarios")
  class InvalidHashScenarios {

    @Test
    @DisplayName("Throws exception when hash is null")
    void throwsExceptionWhenHashIsNull() {
      assertThrows(InvalidPasswordException.class, () -> new PasswordHash(null));
    }

    @Test
    @DisplayName("Throws exception when hash does not start with $2")
    void throwsExceptionWhenHashDoesNotStartWithPrefix() {
      assertThrows(InvalidPasswordException.class, () -> new PasswordHash("invalidHash"));
    }
  }
}
