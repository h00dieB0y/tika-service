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
import engineer.mkitsoukou.tika.domain.model.valueobject.Email;
import engineer.mkitsoukou.tika.domain.model.valueobject.PasswordHash;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.PlainPassword;
import engineer.mkitsoukou.tika.domain.model.valueobject.UserId;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import java.time.Instant;
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
   * @param passwordHasher service to hash passwords
   * @param now            the timestamp when the registration occurred
   * @return a new User instance
   * @throws EntityRequiredFieldException if any parameter is null
   */
  public static User register(
      Email email,
      PlainPassword plainPassword,
      PasswordHasher passwordHasher,
      Instant now
  ) {
    requireNonNull(email, "email");
    requireNonNull(plainPassword, "plainPassword");
    requireNonNull(passwordHasher, "passwordHasher");
    requireNonNull(now, "now");

    var newId = UserId.generate();
    var hash = passwordHasher.hash(plainPassword);
    var user = new User(newId, email, hash, Collections.emptySet());
    user.recordEvent(UserRegistered.createEvent(newId, email, now));
    return user;
  }

  /**
   * Factory method to register a new user with the given email and password using current timestamp.
   *
   * @deprecated Use {@link #register(Email, PlainPassword, PasswordHasher, Instant)} with explicit timestamp for deterministic behavior
   * @param email           the email address of the new user
   * @param plainPassword   the plain text password of the new user
   * @param passwordHasher service to hash passwords
   * @return a new User instance
   * @throws EntityRequiredFieldException if any parameter is null
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public static User register(
      Email email,
      PlainPassword plainPassword,
      PasswordHasher passwordHasher
  ) {
    return register(email, plainPassword, passwordHasher, Instant.now());
  }

  /**
   * Changes the user's password after verifying the old password.
   *
   * @param oldPassword     the current password for verification
   * @param newPassword     the new password to set
   * @param passwordHasher service to hash and verify passwords
   * @param now            the timestamp when the password was changed
   * @throws EntityRequiredFieldException if any parameter is null
   * @throws IncorrectPasswordException if old password is incorrect
   */
  public void changePassword(
      PlainPassword oldPassword,
      PlainPassword newPassword,
      PasswordHasher passwordHasher,
      Instant now
  ) {
    requireNonNull(oldPassword, "oldPassword");
    requireNonNull(newPassword, "newPassword");
    requireNonNull(passwordHasher, "passwordHasher");
    requireNonNull(now, "now");

    if (!passwordHasher.match(oldPassword, passwordHash)) {
      throw new IncorrectPasswordException();
    }

    this.passwordHash = passwordHasher.hash(newPassword);
    recordEvent(PasswordChanged.createEvent(id, now));
  }

  /**
   * Changes the user's password after verifying the old password using current timestamp.
   *
   * @deprecated Use {@link #changePassword(PlainPassword, PlainPassword, PasswordHasher, Instant)} with explicit timestamp for deterministic behavior
   * @param oldPassword     the current password for verification
   * @param newPassword     the new password to set
   * @param passwordHasher service to hash and verify passwords
   * @throws EntityRequiredFieldException if any parameter is null
   * @throws IncorrectPasswordException if old password is incorrect
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void changePassword(
      PlainPassword oldPassword,
      PlainPassword newPassword,
      PasswordHasher passwordHasher
  ) {
    changePassword(oldPassword, newPassword, passwordHasher, Instant.now());
  }

  /**
   * Resets the user's password directly without requiring the old password.
   * This is typically used for administrative password resets or forgotten password flows.
   *
   * @param newPassword     the new password to set
   * @param passwordHasher service to hash passwords
   * @param now            the timestamp when the password was reset
   * @throws EntityRequiredFieldException if any parameter is null
   */
  public void resetPassword(
      PlainPassword newPassword,
      PasswordHasher passwordHasher,
      Instant now
  ) {
    requireNonNull(newPassword, "newPassword");
    requireNonNull(passwordHasher, "passwordHasher");
    requireNonNull(now, "now");

    this.passwordHash = passwordHasher.hash(newPassword);
    recordEvent(PasswordChanged.createEvent(id, now));
  }

  /**
   * Resets the user's password directly without requiring the old password using current timestamp.
   *
   * @deprecated Use {@link #resetPassword(PlainPassword, PasswordHasher, Instant)} with explicit timestamp for deterministic behavior
   * @param newPassword     the new password to set
   * @param passwordHasher service to hash passwords
   * @throws EntityRequiredFieldException if any parameter is null
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void resetPassword(
      PlainPassword newPassword,
      PasswordHasher passwordHasher
  ) {
    resetPassword(newPassword, passwordHasher, Instant.now());
  }

  /**
   * Assigns a role to the user if not already assigned.
   *
   * @param role the role to assign
   * @param now  the timestamp when the role was assigned
   * @throws EntityRequiredFieldException if the role is null
   */
  public void assignRole(Role role, Instant now) {
    requireNonNull(role, "role");
    requireNonNull(now, "now");

    if (roles.add(role)) {
      recordEvent(RoleAssigned.createEvent(id, role.getRoleId(), now));
    }
  }

  /**
   * Assigns a role to the user if not already assigned using current timestamp.
   *
   * @deprecated Use {@link #assignRole(Role, Instant)} with explicit timestamp for deterministic behavior
   * @param role the role to assign
   * @throws EntityRequiredFieldException if the role is null
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void assignRole(Role role) {
    assignRole(role, Instant.now());
  }

  /**
   * Removes a role from the user.
   *
   * @param role the role to remove
   * @param now  the timestamp when the role was removed
   * @throws EntityRequiredFieldException if the role is null
   * @throws RoleNotFoundException if the role is not assigned to the user
   * @throws NoRolesAssignedException if removing this role would leave the user without any roles
   */
  public void removeRole(Role role, Instant now) {
    requireNonNull(role, "role");
    requireNonNull(now, "now");

    if (!roles.contains(role)) {
      throw new RoleNotFoundException(role.getRoleId().toString());
    }

    if (roles.size() == 1) {
      throw new NoRolesAssignedException();
    }

    roles.remove(role);
    recordEvent(RoleRemoved.createEvent(id, role.getRoleId(), now));
  }

  /**
   * Removes a role from the user using current timestamp.
   *
   * @deprecated Use {@link #removeRole(Role, Instant)} with explicit timestamp for deterministic behavior
   * @param role the role to remove
   * @throws EntityRequiredFieldException if the role is null
   * @throws RoleNotFoundException if the role is not assigned to the user
   * @throws NoRolesAssignedException if removing this role would leave the user without any roles
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void removeRole(Role role) {
    removeRole(role, Instant.now());
  }

  /**
   * Assigns multiple roles to the user at once.
   * Only roles that are not already assigned will be added.
   * This method is a bulk operation equivalent to calling assignRole() for each role,
   * but with better performance since it processes them as a batch.
   *
   * @param rolesToAssign the set of roles to assign
   * @param now           the timestamp when the roles were assigned
   * @throws EntityRequiredFieldException if the roles parameter is null
   */
  public void assignRoles(Set<Role> rolesToAssign, Instant now) {
    requireNonNull(rolesToAssign, "rolesToAssign");
    requireNonNull(now, "now");

    for (Role role : rolesToAssign) {
      assignRole(role, now);
    }
  }

  /**
   * Assigns multiple roles to the user at once using current timestamp.
   *
   * @deprecated Use {@link #assignRoles(Set, Instant)} with explicit timestamp for deterministic behavior
   * @param rolesToAssign the set of roles to assign
   * @throws EntityRequiredFieldException if the roles parameter is null
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void assignRoles(Set<Role> rolesToAssign) {
    assignRoles(rolesToAssign, Instant.now());
  }

  /**
   * Removes multiple roles from the user at once.
   * Checks that at least one role will remain after the operation.
   * This method is a bulk operation equivalent to calling removeRole() for each role,
   * but with additional safety checks to ensure user integrity.
   *
   * <p>The method performs validation in this order:
   * 1. Checks that all roles to remove exist for the user
   * 2. Verifies that removing these roles won't leave the user without any roles
   * 3. Performs the removal and records events for each removed role
   *</p>
   *
   * @param rolesToRemove the set of roles to remove
   * @param now           the timestamp when the roles were removed
   *
   * @throws EntityRequiredFieldException if the roles parameter is null
   * @throws NoRolesAssignedException if removing these roles would leave the user without any roles
   * @throws RoleNotFoundException if any of the roles is not assigned to this user
   */
  public void removeRoles(Set<Role> rolesToRemove, Instant now) {
    requireNonNull(rolesToRemove, "rolesToRemove");
    requireNonNull(now, "now");

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
      recordEvent(RoleRemoved.createEvent(id, role.getRoleId(), now));
    }
  }

  /**
   * Removes multiple roles from the user at once using current timestamp.
   *
   * @deprecated Use {@link #removeRoles(Set, Instant)} with explicit timestamp for deterministic behavior
   * @param rolesToRemove the set of roles to remove
   * @throws EntityRequiredFieldException if the roles parameter is null
   * @throws NoRolesAssignedException if removing these roles would leave the user without any roles
   * @throws RoleNotFoundException if any of the roles is not assigned to this user
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void removeRoles(Set<Role> rolesToRemove) {
    removeRoles(rolesToRemove, Instant.now());
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
   *
   * @param now the timestamp when the user was activated
   */
  public void activate(Instant now) {
    requireNonNull(now, "now");
    this.active = true;
    recordEvent(UserActivationChanged.createEvent(id, true, now));
  }

  /**
   * Activates the user, allowing them to log in and perform actions using current timestamp.
   *
   * @deprecated Use {@link #activate(Instant)} with explicit timestamp for deterministic behavior
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void activate() {
    activate(Instant.now());
  }

  /**
   * Deactivates the user, preventing them from logging in or performing actions.
   *
   * @param now the timestamp when the user was deactivated
   */
  public void deactivate(Instant now) {
    requireNonNull(now, "now");
    this.active = false;
    recordEvent(UserActivationChanged.createEvent(id, false, now));
  }

  /**
   * Deactivates the user, preventing them from logging in or performing actions using current timestamp.
   *
   * @deprecated Use {@link #deactivate(Instant)} with explicit timestamp for deterministic behavior
   */
  @Deprecated(since = "0.1.0", forRemoval = true)
  public void deactivate() {
    deactivate(Instant.now());
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
