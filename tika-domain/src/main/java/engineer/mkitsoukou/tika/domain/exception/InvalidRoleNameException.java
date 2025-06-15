package engineer.mkitsoukou.tika.domain.exception;

public class InvalidRoleNameException extends DomainException {
  public InvalidRoleNameException(String roleName, Throwable cause) {
    super("role name '%s' is invalid", roleName);
    initCause(cause);
  }
}
