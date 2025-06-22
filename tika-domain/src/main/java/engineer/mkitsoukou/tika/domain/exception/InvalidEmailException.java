package engineer.mkitsoukou.tika.domain.exception;

public class InvalidEmailException extends DomainException {
  public InvalidEmailException(String email, Throwable cause) {
    super("Invalid email '%s': %s", email, cause.getMessage());
    initCause(cause);
  }
}
