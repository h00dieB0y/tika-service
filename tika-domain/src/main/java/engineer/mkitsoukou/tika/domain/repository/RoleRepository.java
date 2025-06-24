package engineer.mkitsoukou.tika.domain.repository;

import engineer.mkitsoukou.tika.domain.model.entity.Role;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
  Optional<Role> findById(RoleId roleId);
  Optional<Role> findByName(RoleName roleName);
  List<Role> findAll();
  void save(Role role);
}
