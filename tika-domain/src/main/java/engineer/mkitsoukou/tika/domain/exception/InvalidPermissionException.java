package engineer.mkitsoukou.tika.domain.exception;

public class InvalidPermissionException extends DomainException {
  public InvalidPermissionException(String permission, Throwable cause) {
    super("Invalid permission '%s': %s", permission, cause.getMessage());
    initCause(cause);
  }
}
