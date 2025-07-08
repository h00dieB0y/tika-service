package engineer.mkitsoukou.tika.application.auth.port.out;

import java.time.Instant;

public interface RefreshTokenStorePort {
  /**
   * Persist RT metadata on issue; used to revoke / rotate.
   *
   * @param userId       User ID to which the RT belongs.
   * @param refreshToken The refresh token itself.
   * @param expiresAt    Expiration time of the refresh token.
   */
  void store(String userId, String refreshToken, Instant expiresAt);

  /**
   * True if RT is still valid (not revoked/rotated/expired).
   *
   * @param userId       User ID to which the RT belongs.
   * @param refreshToken The refresh token itself.
   */
  boolean isValid(String userId, String refreshToken);

  /**
   * Revoke old RT on rotation/logout.
   *
   * @param userId       User ID to which the RT belongs.
   * @param refreshToken The refresh token itself.
   */
  void revoke(String userId, String refreshToken);
}
