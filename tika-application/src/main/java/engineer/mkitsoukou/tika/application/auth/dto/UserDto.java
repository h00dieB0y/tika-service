package engineer.mkitsoukou.tika.application.auth.dto;

import engineer.mkitsoukou.tika.domain.model.entity.User;

/**
 * Data returned to adapters after a successful registration.
 * Only exposes safe, non-sensitive information.
 */
public record UserDto(String id, String email) {

  public static UserDto from(User user) {
    return new UserDto(user.getId().value().toString(), user.getEmail().value());
  }
}
