package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleNameException;
import java.util.regex.Pattern;

public record RoleName(String name) {

  private static final Pattern PATTERN = Pattern.compile("^(?:ROLE_)?[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$");
  private static final int MAX_LENGTH = 100;

  public RoleName {
    if (name == null || name.isBlank()) {
      throw new InvalidRoleNameException(name, new IllegalArgumentException("Role name must not be null or blank"));
    }

    if (name.length() > MAX_LENGTH) {
      throw new InvalidRoleNameException(name, new IllegalArgumentException("Role name must not exceed " + MAX_LENGTH + " characters"));
    }

    if (!PATTERN.matcher(name).matches()) {
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
