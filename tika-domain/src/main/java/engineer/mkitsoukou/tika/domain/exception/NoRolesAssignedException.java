package engineer.mkitsoukou.tika.domain.exception;

/**
 * Exception thrown when attempting to remove a role from a user that would leave the user without any roles.
 */
public class NoRolesAssignedException extends DomainException {
  public NoRolesAssignedException() {
    super("User must have at least one role assigned");
  }
}
