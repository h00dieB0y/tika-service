package engineer.mkitsoukou.tika.domain.repository;

import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
  Optional<User> findById(UserId userId);
  Optional<User> findByEmail(Email email);
  void save(User user);
}
