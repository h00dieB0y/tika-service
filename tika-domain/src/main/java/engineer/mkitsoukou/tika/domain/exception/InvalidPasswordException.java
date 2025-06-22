package engineer.mkitsoukou.tika.domain.exception;

public class InvalidPasswordException extends DomainException {

  public InvalidPasswordException(String password, Throwable cause) {
    super("Invalid password '%s': %s", password, cause.getMessage());
    initCause(cause);
  }
}
