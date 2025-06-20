package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.model.event.PasswordChanged;
import engineer.mkitsoukou.tika.domain.model.event.RoleAssigned;
import engineer.mkitsoukou.tika.domain.model.event.RoleRemoved;
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

  /**
   * Creates a new user with the specified details.
   *
   * @param id           the unique identifier of the user
   * @param email        the email address of the user
   * @param passwordHash the hashed password of the user
   * @param initialRoles the initial set of roles assigned to the user
   * @throws IllegalArgumentException if any parameter is null
   */
  private User(UserId id, Email email, PasswordHash passwordHash, Set<Role> initialRoles) {
    this.id = requireNonNull(id, "id");
    this.email = requireNonNull(email, "email");
    this.passwordHash = requireNonNull(passwordHash, "passwordHash");
    this.roles = new LinkedHashSet<>(requireNonNull(initialRoles, "initialRoles"));
  }

  /**
   * Factory method to register a new user with the given email and password.
   *
   * @param email           the email address of the new user
   * @param plainPassword   the plain text password of the new user
   * @param passwordService service to hash passwords
   * @return a new User instance
   * @throws IllegalArgumentException if any parameter is null
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
    user.recordEvent(UserRegistered.of(newId, email));
    return user;
  }

  /**
   * Changes the user's password after verifying the old password.
   *
   * @param oldPassword     the current password for verification
   * @param newPassword     the new password to set
   * @param passwordService service to hash and verify passwords
   * @throws IllegalArgumentException if old password is incorrect or any parameter is null
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
      throw new IllegalArgumentException("Old password is incorrect");
    }

    this.passwordHash = passwordService.hash(newPassword);
    recordEvent(PasswordChanged.of(id));
  }

  /**
   * Assigns a role to the user if not already assigned.
   *
   * @param role the role to assign
   * @throws IllegalArgumentException if the role is null
   */
  public void assignRole(Role role) {
    requireNonNull(role, "role");

    if (roles.add(role)) {
      recordEvent(RoleAssigned.of(id, role.getRoleId()));
    }
  }

  /**
   * Removes a role from the user.
   *
   * @param role the role to remove
   * @throws IllegalArgumentException if the role is null or not assigned to the user
   */
  public void removeRole(Role role) {
    requireNonNull(role, "role");

    if (!roles.remove(role)) {
      throw new IllegalArgumentException("Role not assigned: " + role.getRoleId());
    }
    recordEvent(RoleRemoved.of(id, role.getRoleId()));
  }

  /**
   * Checks if the user has a specific role.
   *
   * @param role the role to check
   * @return true if the user has the role, false otherwise
   * @throws IllegalArgumentException if the role is null
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
   * @throws IllegalArgumentException if the permission is null
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

