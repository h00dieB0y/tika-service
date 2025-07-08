package engineer.mkitsoukou.tika.application.auth.port.out;

public interface TokenBlacklistPort {

  /**
   * Check if the JTI (JWT ID) is already black-listed.
   * @return {@code true} if the JTI is already black-listed.
   */
  boolean isBlacklisted(String jti);

  /**
   * Persist the JTI so any future introspection fails.
   * Idempotent: calling twice has the same effect as once.
   */
  void blacklist(String jti);
}
