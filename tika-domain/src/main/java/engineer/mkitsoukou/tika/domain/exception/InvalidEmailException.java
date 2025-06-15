package engineer.mkitsoukou.tika.domain.exception;

public class InvalidEmailException extends DomainException {
  public InvalidEmailException(String email, Throwable cause) {
    super("email '%s' is malformed", email);
    initCause(cause);
  }
}
