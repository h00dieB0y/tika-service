package engineer.mkitsoukou.tika.domain.exception;

public class InvalidUserIdException extends DomainException {

  public InvalidUserIdException(String userId, Throwable cause) {
    super("user id '%s' is malformed", userId);
    initCause(cause);
  }
}
