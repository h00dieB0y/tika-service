package engineer.mkitsoukou.tika.application.auth.exception;

/**
 * Thrown when the supplied email / password combination is invalid.
 *
 * <p>Maps to <strong>HTTP 401 Unauthorized</strong> in the REST adapter.</p>
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}
