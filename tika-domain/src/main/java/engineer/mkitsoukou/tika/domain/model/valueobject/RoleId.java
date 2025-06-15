package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleIdException;
import java.util.Objects;
import java.util.UUID;

public record RoleId(UUID value) {

  public RoleId {
    Objects.requireNonNull(value, "role id cannot be null");
  }

  public static RoleId of(UUID id) {
    return new RoleId(id);
  }

  public static RoleId of(String id) {
    try {
      return new RoleId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new InvalidRoleIdException(id, e);
    }
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
