package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionException;
import java.util.regex.Pattern;

public record Permission(String value) {

  private static final Pattern PERMISSION_PATTERN = Pattern.compile(
      "^[a-z][a-z0-9-]*(?:\\.[a-z][a-z0-9-]*)+$");

  public Permission {
    if (value == null || value.isBlank()) {
      throw new InvalidPermissionException(
        value,
          new IllegalArgumentException("Permission must not be null or blank")
      );
    }

    if (value.length() > 100) {
      throw new InvalidPermissionException(
        value,
          new IllegalArgumentException("Permission must not exceed 100 characters")
      );
    }

    if (!PERMISSION_PATTERN.matcher(value).matches()) {
      throw new InvalidPermissionException(
        value,
          new IllegalArgumentException("must be a valid permission format")
      );
    }
  }

  public static Permission of(String permission) {
    return new Permission(permission);
  }

  @Override
  public String toString() {
    return value;
  }
}
