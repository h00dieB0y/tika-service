package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.exception.IncorrectPasswordException;
import engineer.mkitsoukou.tika.domain.exception.NoRolesAssignedException;
import engineer.mkitsoukou.tika.domain.exception.RoleNotFoundException;
import engineer.mkitsoukou.tika.domain.model.event.PasswordChanged;
import engineer.mkitsoukou.tika.domain.model.event.RoleAssigned;
import engineer.mkitsoukou.tika.domain.model.event.RoleRemoved;
import engineer.mkitsoukou.tika.domain.model.event.UserActivationChanged;
import engineer.mkitsoukou.tika.domain.model.event.UserRegistered;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.service.PasswordService;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the system with authentication details and assigned roles.
 * Users can perform actions based on the permissions granted by their roles.
 */
public final class User extends AbstractEntity {
  private final UserId id;
  private final Email email;
  private final Set<Role> roles;
  private PasswordHash passwordHash;
  private boolean active;

  /**
   * Creates a new user with the specified details.
   *
   * @param id           the unique identifier of the user
   * @param email        the email address of the user
   * @param passwordHash the hashed password of the user
   * @param initialRoles the initial set of roles assigned to the user
   * @throws EntityRequiredFieldException if any parameter is null
   */
  private User(UserId id, Email email, PasswordHash passwordHash, Set<Role> initialRoles) {
    this.id = requireNonNull(id, "id");
    this.email = requireNonNull(email, "email");
    this.passwordHash = requireNonNull(passwordHash, "passwordHash");
    this.roles = new LinkedHashSet<>(requireNonNull(initialRoles, "initialRoles"));
    this.active = true; // Default to active
  }

  /**
   * Factory method to register a new user with the given email and password.
   *
   * @param email           the email address of the new user
   * @param plainPassword   the plain text password of the new user
   * @param passwordService service to hash passwords
   * @return a new User instance
   * @throws EntityRequiredFieldException if any parameter is null
   */
  public static User register(
      Email email,
      PlainPassword plainPassword,
      PasswordService passwordService
  ) {
    requireNonNull(email, "email");
    requireNonNull(plainPassword, "plainPassword");
    requireNonNull(passwordService, "passwordService");

    var newId = UserId.generate();
    var hash = passwordService.hash(plainPassword);
    var user = new User(newId, email, hash, Collections.emptySet());
    user.recordEvent(UserRegistered.createEvent(newId, email));
    return user;
  }

  /**
   * Changes the user's password after verifying the old password.
   *
   * @param oldPassword     the current password for verification
   * @param newPassword     the new password to set
   * @param passwordService service to hash and verify passwords
   * @throws EntityRequiredFieldException if any parameter is null
   * @throws IncorrectPasswordException if old password is incorrect
   */
  public void changePassword(
      PlainPassword oldPassword,
      PlainPassword newPassword,
      PasswordService passwordService
  ) {
    requireNonNull(oldPassword, "oldPassword");
    requireNonNull(newPassword, "newPassword");
    requireNonNull(passwordService, "passwordService");

    if (!passwordService.match(oldPassword, passwordHash)) {
      throw new IncorrectPasswordException();
    }

    this.passwordHash = passwordService.hash(newPassword);
    recordEvent(PasswordChanged.createEvent(id));
  }

  /**
   * Resets the user's password directly without requiring the old password.
   * This is typically used for administrative password resets or forgotten password flows.
   *
   * @param newPassword     the new password to set
   * @param passwordService service to hash passwords
   * @throws EntityRequiredFieldException if any parameter is null
   */
  public void resetPassword(
      PlainPassword newPassword,
      PasswordService passwordService
  ) {
    requireNonNull(newPassword, "newPassword");
    requireNonNull(passwordService, "passwordService");

    this.passwordHash = passwordService.hash(newPassword);
    recordEvent(PasswordChanged.createEvent(id));
  }

  /**
   * Assigns a role to the user if not already assigned.
   *
   * @param role the role to assign
   * @throws EntityRequiredFieldException if the role is null
   */
  public void assignRole(Role role) {
    requireNonNull(role, "role");

    if (roles.add(role)) {
      recordEvent(RoleAssigned.createEvent(id, role.getRoleId()));
    }
  }

  /**
   * Removes a role from the user.
   *
   * @param role the role to remove
   * @throws EntityRequiredFieldException if the role is null
   * @throws RoleNotFoundException if the role is not assigned to the user
   * @throws NoRolesAssignedException if removing this role would leave the user withhout any roles
   */
  public void removeRole(Role role) {
    requireNonNull(role, "role");

    if (!roles.contains(role)) {
      throw new RoleNotFoundException(role.getRoleId().toString());
    }

    if (roles.size() <= 1) {
      throw new NoRolesAssignedException();
    }

    roles.remove(role);
    recordEvent(RoleRemoved.createEvent(id, role.getRoleId()));
  }

  /**
   * Assigns multiple roles to the user at once.
   * Only roles that are not already assigned will be added.
   * This method is a bulk operation equivalent to calling assignRole() for each role,
   * but with better performance since it processes them as a batch.
   *
   * @param rolesToAssign the set of roles to assign
   * @throws EntityRequiredFieldException if the roles parameter is null
   */
  public void assignRoles(Set<Role> rolesToAssign) {
    requireNonNull(rolesToAssign, "rolesToAssign");

    for (Role role : rolesToAssign) {
      assignRole(role);
    }
  }

  /**
   * Removes multiple roles from the user at once.
   * Checks that at least one role will remain after the operation.
   * This method is a bulk operation equivalent to calling removeRole() for each role,
   * but with additional safety checks to ensure user integrity.
   *
   * The method performs validation in this order:
   * 1. Checks that all roles to remove exist for the user
   * 2. Verifies that removing these roles won't leave the user without any roles
   * 3. Performs the removal and records events for each removed role
   *
   * @param rolesToRemove the set of roles to remove
   * @throws EntityRequiredFieldException if the roles parameter is null
   * @throws NoRolesAssignedException if removing these roles would leave the user without any roles
   * @throws RoleNotFoundException if any of the roles is not assigned to this user
   */
  public void removeRoles(Set<Role> rolesToRemove) {
    requireNonNull(rolesToRemove, "rolesToRemove");

    // Check that all roles exist
    for (Role role : rolesToRemove) {
      if (!roles.contains(role)) {
        throw new RoleNotFoundException(role.getRoleId().toString());
      }
    }

    // Check that we'll have at least one role left
    if (roles.size() <= rolesToRemove.size()) {
      throw new NoRolesAssignedException();
    }

    // Remove all roles
    for (Role role : rolesToRemove) {
      roles.remove(role);
      recordEvent(RoleRemoved.createEvent(id, role.getRoleId()));
    }
  }

  /**
   * Checks if the user has a specific role.
   *
   * @param role the role to check
   * @return true if the user has the role, false otherwise
   * @throws EntityRequiredFieldException if the role is null
   */
  public boolean hasRole(Role role) {
    requireNonNull(role, "role");
    return roles.contains(role);
  }

  /**
   * Checks if the user has a specific permission through any of their roles.
   *
   * @param permission the permission to check
   * @return true if the user has the permission, false otherwise
   * @throws EntityRequiredFieldException if the permission is null
   */
  public boolean hasPermission(Permission permission) {
    requireNonNull(permission, "permission");
    return roles.stream().anyMatch(role -> role.hasPermission(permission));
  }

  /**
   * Gets the unique identifier of this user.
   *
   * @return the user ID
   */
  public UserId getId() {
    return id;
  }

  /**
   * Gets the email address of this user.
   *
   * @return the email address
   */
  public Email getEmail() {
    return email;
  }

  /**
   * Gets the hashed password of this user.
   *
   * @return the password hash
   */
  public PasswordHash getPasswordHash() {
    return passwordHash;
  }

  /**
   * Gets an unmodifiable view of the roles assigned to this user.
   *
   * @return the set of roles
   */
  public Set<Role> getRoles() {
    return Set.copyOf(roles);
  }

  /**
   * Checks if the user is active.
   *
   * @return true if the user is active, false otherwise
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Activates the user, allowing them to log in and perform actions.
   */
  public void activate() {
    this.active = true;
    recordEvent(UserActivationChanged.createEvent(id, true));
  }

  /**
   * Deactivates the user, preventing them from logging in or performing actions.
   */
  public void deactivate() {
    this.active = false;
    recordEvent(UserActivationChanged.createEvent(id, false));
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof User that && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "User[id=" + id + ", email=" + email + ", roles=" + roles.size() + "]";
  }
}
