package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import java.util.regex.Pattern;

/**
 * Represents a plain password with validation rules.
 * A valid password must:
 * - Be between 8 and 64 characters long
 * - Contain at least one uppercase letter
 * - Contain at least one lowercase letter
 * - Contain at least one digit
 * - Contain at least one special character
 */
public record PlainPassword(String clearText) {
  private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
  private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
  private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
  private static final int MIN_LENGTH = 8;
  private static final int MAX_LENGTH = 64;

  public PlainPassword {
    clearText = clearText == null ? null : clearText.trim();

    if (clearText == null || clearText.isBlank()) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must not be null or blank"));
    }

    if (clearText.length() < MIN_LENGTH || clearText.length() > MAX_LENGTH) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long"));
    }

    if (!UPPERCASE_PATTERN.matcher(clearText).find()) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must contain at least one uppercase letter"));
    }

    if (!LOWERCASE_PATTERN.matcher(clearText).find()) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must contain at least one lowercase letter"));
    }

    if (!DIGIT_PATTERN.matcher(clearText).find()) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must contain at least one digit"));
    }

    if (!SPECIAL_CHAR_PATTERN.matcher(clearText).find()) {
      throw new InvalidPasswordException(clearText, new IllegalArgumentException("must contain at least one special character"));
    }
  }

  public static PlainPassword of(String password) {
    return new PlainPassword(password);
  }

  @Override
  public String toString() {
    return clearText;
  }
}
