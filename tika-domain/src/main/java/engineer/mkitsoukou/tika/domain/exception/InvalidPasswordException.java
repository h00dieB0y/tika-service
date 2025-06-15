package engineer.mkitsoukou.tika.domain.exception;

public class InvalidPasswordException extends DomainException {

  public InvalidPasswordException(String password, Throwable cause) {
    super("password '%s' is invalid", password);
    initCause(cause);
  }
}
