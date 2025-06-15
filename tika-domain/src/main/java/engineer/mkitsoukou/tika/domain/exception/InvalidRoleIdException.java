package engineer.mkitsoukou.tika.domain.exception;

public class InvalidRoleIdException extends DomainException {

  public InvalidRoleIdException(String roleId, Throwable cause) {
    super("role id '%s' is malformed", roleId);
    initCause(cause);
  }
}
