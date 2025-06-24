package engineer.mkitsoukou.tika.domain.repository;

import engineer.mkitsoukou.tika.domain.model.entity.User;
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods for CRUD operations and querying user data.
 *
 * <p>Implementations of this interface should handle transaction boundaries appropriately.
 * Methods that modify data ({@code save}, {@code delete}, {@code deleteById}) should be executed
 * within a transaction to ensure data consistency.</p>
 */
public interface UserRepository {
  /**
   * Finds a user by their unique identifier.
   *
   * @param userId the unique identifier of the user
   * @return an Optional containing the user if found, or empty Optional if not found
   * @implNote This operation should be executed within a read-only transaction context
   */
  Optional<User> findById(UserId userId);

  /**
   * Finds a user by their email address.
   *
   * @param email the email address of the user to find
   * @return an Optional containing the user if found, or empty Optional if not found
   * @implNote This operation should be executed within a read-only transaction context
   */
  Optional<User> findByEmail(Email email);

  /**
   * Retrieves all users in the system.
   *
   * @return a list of all users
   * @implNote This operation should be executed within a read-only transaction context
   */
  List<User> findAll();

  /**
   * Persists a user entity to the repository.
   * This method can be used for both creating new users and updating existing ones.
   *
   * @param user the user entity to save
   * @return an Optional containing the saved user if successful, or empty Optional if the operation failed
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  Optional<User> save(User user);

  /**
   * Deletes a user entity from the repository.
   *
   * @param user the user entity to delete
   * @return true if the user was successfully deleted, false otherwise
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  boolean delete(User user);

  /**
   * Deletes a user by their unique identifier.
   *
   * @param userId the unique identifier of the user to delete
   * @return true if the user was successfully deleted, false if the user wasn't found or couldn't be deleted
   * @implNote This operation should be executed within a transaction context to ensure data consistency
   */
  boolean deleteById(UserId userId);

  /**
   * Checks if a user with the specified ID exists.
   *
   * @param userId the unique identifier to check
   * @return true if a user with the given ID exists, false otherwise
   * @implNote This operation should be executed within a read-only transaction context
   */
  boolean existsById(UserId userId);

  /**
   * Checks if a user with the specified email exists.
   *
   * @param email the email address to check
   * @return true if a user with the given email exists, false otherwise
   * @implNote This operation should be executed within a read-only transaction context
   */
  boolean existsByEmail(Email email);

  /**
   * Counts the total number of users in the repository.
   *
   * @return the total count of users
   * @implNote This operation should be executed within a read-only transaction context
   */
  long count();
}
