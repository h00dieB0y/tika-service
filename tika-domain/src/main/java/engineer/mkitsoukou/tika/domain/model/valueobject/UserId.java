package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidUserIdException;
import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "user id cannot be null");
  }

  public static UserId of(UUID id) {
    return new UserId(id);
  }

  public static UserId of(String id) {
    try {
      return new UserId(UUID.fromString(id));
    } catch (IllegalArgumentException e) {
      throw new InvalidUserIdException(id, e);
    }
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
