package engineer.mkitsoukou.tika.domain.exception;

public class InvalidRoleNameException extends DomainException {
  public InvalidRoleNameException(String roleName, Throwable cause) {
    super("Invalid role name '%s': %s", roleName, cause.getMessage());
    initCause(cause);
  }
}
