package engineer.mkitsoukou.tika.domain.repository;

import engineer.mkitsoukou.tika.domain.model.entity.Role;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleId;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities.
 * Provides methods for CRUD operations and querying role data.
 *
 * <p>Implementations of this interface should handle transaction boundaries appropriately.
 * Methods that modify data ({@code save}, {@code delete}, {@code deleteById}) should be executed
 * within a transaction to ensure data consistency.</p>
 */
public interface RoleRepository {
  /**
   * Finds a role by its unique identifier.
   *
   * @param roleId the unique identifier of the role
   * @return an Optional containing the role if found, or empty Optional if not found
   * @implNote This operation should be executed within a read-only transaction context
   */
  Optional<Role> findById(RoleId roleId);

  /**
   * Finds a role by its name.
   *
   * @param roleName the name of the role to find
   * @return an Optional containing the role if found, or empty Optional if not found
   * @implNote This operation should be executed within a read-only transaction context
   */
  Optional<Role> findByName(RoleName roleName);

  /**
   * Retrieves all roles in the system.
   *
   * @return a list of all roles
   * @implNote This operation should be executed within a read-only transaction context
   */
  List<Role> findAll();

  /**
   * Persists a role entity to the repository.
   * This method can be used for both creating new roles and updating existing ones.
   *
   * @param role the role entity to save
   * @return an Optional containing the saved role if successful, or empty Optional if the operation failed
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  Optional<Role> save(Role role);

  /**
   * Deletes a role entity from the repository.
   *
   * @param role the role entity to delete
   * @return true if the role was successfully deleted, false otherwise
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  boolean delete(Role role);

  /**
   * Deletes a role by its unique identifier.
   *
   * @param roleId the unique identifier of the role to delete
   * @return true if the role was successfully deleted, false if the role wasn't found or couldn't be deleted
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  boolean deleteById(RoleId roleId);

  /**
   * Checks if a role with the specified ID exists.
   *
   * @param roleId the unique identifier to check
   * @return true if a role with the given ID exists, false otherwise
   * @implNote This operation should be executed within a read-only transaction context
   */
  boolean existsById(RoleId roleId);

  /**
   * Checks if a role with the specified name exists.
   *
   * @param roleName the role name to check
   * @return true if a role with the given name exists, false otherwise
   * @implNote This operation should be executed within a read-only transaction context
   */
  boolean existsByName(RoleName roleName);

  /**
   * Counts the total number of roles in the repository.
   *
   * @return the total count of roles
   * @implNote This operation should be executed within a read-only transaction context
   */
  long count();
}
