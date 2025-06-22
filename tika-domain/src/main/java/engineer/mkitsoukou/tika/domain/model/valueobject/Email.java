package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidEmailException;
import java.util.regex.Pattern;

public record Email(
    String value
) {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?){1,10}$"
  );

  public Email {
    value = value == null ? null : value.trim();

    if (value == null || value.isBlank()) {
      throw new InvalidEmailException(value, new IllegalArgumentException("Email must not be null or blank"));
    }

    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new InvalidEmailException(value, new IllegalArgumentException("must be a valid email format"));
    }
  }

  public static Email of(String email) {
    return new Email(email);
  }

  @Override
  public String toString() {
    return value;
  }
}
