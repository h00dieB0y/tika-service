package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionNameException;
import java.util.regex.Pattern;

public record PermissionName(String name) {

  private static final Pattern PERMISSION_NAME_PATTERN = Pattern.compile(
      "^[a-z][a-z0-9-]*(?:\\.[a-z][a-z0-9-]*)+$");


  public PermissionName {
    if (name == null || name.isBlank()) {
      throw new InvalidPermissionNameException(
        name,
          new IllegalArgumentException("Permission name must not be null or blank")
      );
    }

    if (name.length() > 100) {
      throw new InvalidPermissionNameException(
        name,
          new IllegalArgumentException("Permission name must not exceed 100 characters")
      );
    }

    if (!PERMISSION_NAME_PATTERN.matcher(name).matches()) {
      throw new InvalidPermissionNameException(
        name,
          new IllegalArgumentException("must be a valid permission name format")
      );
    }
  }
}
