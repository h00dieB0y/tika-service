package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;

/**
 * Represents a hashed password.
 * <p>
 * This value object encapsulates the password hash and ensures that it adheres to the expected format.
 * The hash must start with "$2" to indicate that it is a valid bcrypt hash.
 * * @param hash the hashed password, must start with "$2"
 * * @throws InvalidPasswordException if the hash is null or does not start with "$2"
 * * <p>
 */
public record PasswordHash(String hash) {

  /**
   * Constructs a PasswordHash object.
   *
   * @param hash the hashed password, must start with "$2"
   * @throws InvalidPasswordException if the hash is null or does not start with "$2"
   */
  public PasswordHash {
    if (hash == null || !hash.startsWith("$2")) {
      throw new InvalidPasswordException(
        hash,
          new IllegalArgumentException("must be a valid hash format"));
    }
  }

  @Override
  public String toString() {
    return "[PROTECTED]"; // Masking the hash for security reasons
  }
}
