package engineer.mkitsoukou.tika.application.auth.exception;

/**
 * Thrown when a user account exists but is disabled, locked or not yet verified.
 *
 * <p>Maps to <strong>HTTP 403 Forbidden</strong>.</p>
 */
public class UserInactiveException extends RuntimeException {

  public UserInactiveException() {
    super("User account is inactive");
  }
}
