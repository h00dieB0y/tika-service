package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleNameException;
import java.util.regex.Pattern;

public record RoleName(String name) {

  // Role names: optional ROLE_ prefix, all-caps, segments separated by single "_".
  // • No lowercase, hyphens, leading digits/underscores, trailing/double underscores
  // • Digits allowed only after the first "_" (or if there is no "_" at all)
  // • Up to 3 segments, each 1-30 characters, total length up to 100 characters
  private static final Pattern ROLE_PATTERN = Pattern.compile(
      "^(?:ROLE_)?[A-Z][A-Z]*(?:\\d+[A-Z]*)*(?:_[A-Z0-9]{1,30}){0,2}$"
  );


  private static final int MAX_LENGTH = 100;

  public RoleName {
    if (name == null || name.isBlank()) {
      throw new InvalidRoleNameException(name, new IllegalArgumentException("Role name must not be null or blank"));
    }

    if (name.length() > MAX_LENGTH) {
      throw new InvalidRoleNameException(name, new IllegalArgumentException("Role name must not exceed " + MAX_LENGTH + " characters"));
    }

    if (!ROLE_PATTERN.matcher(name).matches()) {
      throw new InvalidRoleNameException(name, new IllegalArgumentException("must be a valid role name format"));
    }
  }

  public static RoleName of(String name) {
    return new RoleName(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
