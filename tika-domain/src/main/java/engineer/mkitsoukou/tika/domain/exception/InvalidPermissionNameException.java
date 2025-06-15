package engineer.mkitsoukou.tika.domain.exception;

public class InvalidPermissionNameException extends DomainException {
  public InvalidPermissionNameException(String permissionName, Throwable cause) {
    super("permission name '%s' is invalid", permissionName);
    initCause(cause);
  }
}
