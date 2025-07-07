package engineer.mkitsoukou.tika.application.auth.exception;

/**
 * Raised by the rate-limiter when the client has exceeded the allowed number
 * of login attempts in the configured time-window.
 *
 * <p>Maps to <strong>HTTP 429 Too Many Requests</strong>.</p>
 */
public class TooManyAttemptsException extends RuntimeException {

  private final String email;   // for logging / metrics

  public TooManyAttemptsException(String email) {
    super("Too many login attempts for " + email);
    this.email = email;
  }

  public String email() {
    return email;
  }
}
