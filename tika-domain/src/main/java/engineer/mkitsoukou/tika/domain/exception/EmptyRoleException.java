package engineer.mkitsoukou.tika.domain.exception;

/**
 * Exception thrown when attempting to create or modify a role to have no permissions.
 */
public class EmptyRoleException extends DomainException {
  public EmptyRoleException() {
    super("Role must have at least one permission");
  }
}
