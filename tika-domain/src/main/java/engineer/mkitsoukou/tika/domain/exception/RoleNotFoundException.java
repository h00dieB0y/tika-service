package engineer.mkitsoukou.tika.domain.exception;

public class RoleNotFoundException extends DomainException {
  public RoleNotFoundException(String roleId) {
    super("Role not assigned: %s", roleId);
  }
}
