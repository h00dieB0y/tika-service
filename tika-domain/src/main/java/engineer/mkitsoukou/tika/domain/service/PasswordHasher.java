package engineer.mkitsoukou.tika.domain.service;

import engineer.mkitsoukou.tika.domain.model.valueobject.PasswordHash;
import engineer.mkitsoukou.tika.domain.model.valueobject.PlainPassword;

public interface PasswordHasher {

  /**
   * Hashes a plain password.
   *
   * @param plain the plain password to hash
   * @return the hashed password
   */
  PasswordHash hash(PlainPassword plain);

  /**
   * Matches a plain password against a hashed password.
   *
   * @param plain the plain password to match
   * @param hash the hashed password to match against
   * @return true if the passwords match, false otherwise
   */
  boolean match(PlainPassword plain, PasswordHash hash);
}
