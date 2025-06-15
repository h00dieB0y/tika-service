package engineer.mkitsoukou.tika.domain.exception;

public class InvalidPermissionException extends DomainException {
  public InvalidPermissionException(String permission, Throwable cause) {
    super("permission '%s' is invalid", permission);
    initCause(cause);
  }
}
