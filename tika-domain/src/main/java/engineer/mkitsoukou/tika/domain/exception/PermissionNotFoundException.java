package engineer.mkitsoukou.tika.domain.exception;

/**
 * Exception thrown when attempting to remove a permission that is not assigned to a role.
 */
public class PermissionNotFoundException extends DomainException {
  public PermissionNotFoundException(String permissionName) {
    super("Permission not found: " + permissionName);
  }
}
