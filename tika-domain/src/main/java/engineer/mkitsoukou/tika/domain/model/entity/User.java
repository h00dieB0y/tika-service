package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.model.event.*;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.service.PasswordService;

import java.util.*;

public final class User {
  private final transient List<DomainEvent> events = new ArrayList<>();
  private final UserId id;
  private final Email email;
  private PasswordHash passwordHash;
  private final Set<Role> roles;

  private User(UserId id, Email email, PasswordHash passwordHash, Set<Role> initialRoles) {
    this.id = Objects.requireNonNull(id);
    this.email = Objects.requireNonNull(email);
    this.passwordHash = Objects.requireNonNull(passwordHash);
    this.roles = new LinkedHashSet<>(Objects.requireNonNull(initialRoles));
  }

  public static User register(
      Email email,
      PlainPassword plainPassword,
      PasswordService passwordService
  ) {
    Objects.requireNonNull(email);
    Objects.requireNonNull(plainPassword);
    Objects.requireNonNull(passwordService);

    var newId = UserId.generate();
    var hash = passwordService.hash(plainPassword);
    var user = new User(newId, email, hash, Collections.emptySet());
    user.publish(UserRegistered.of(newId, email));
    return user;
  }

  public void changePassword(
      PlainPassword oldPassword,
      PlainPassword newPassword,
      PasswordService passwordService
  ) {
    Objects.requireNonNull(oldPassword);
    Objects.requireNonNull(newPassword);
    Objects.requireNonNull(passwordService);

    if (!passwordService.match(oldPassword, passwordHash)) {
      throw new IllegalArgumentException("Old password is incorrect");
    }

    this.passwordHash = passwordService.hash(newPassword);
    publish(PasswordChanged.of(id));
  }

  public void assignRole(Role role) {
    Objects.requireNonNull(role);
    if (roles.add(role)) {
      publish(RoleAssigned.of(id, role.getRoleId()));
    }
  }

  public void removeRole(Role role) {
    Objects.requireNonNull(role);
    if (!roles.remove(role)) {
      throw new IllegalArgumentException("Role not assigned: " + role.getRoleId());
    }
    publish(RoleRemoved.of(id, role.getRoleId()));
  }

  public UserId getId()                 { return id; }
  public Email getEmail()               { return email; }
  public PasswordHash getPasswordHash() { return passwordHash; }
  public Set<Role> getRoles()           { return Set.copyOf(roles); }

  public List<DomainEvent> pullEvents() {
    var snapshot = List.copyOf(events);
    events.clear();
    return snapshot;
  }

  private void publish(DomainEvent event) {
    events.add(Objects.requireNonNull(event));
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
    return "User[id=" + id + ", email=" + email + ", roles=" + roles + "]";
  }
}
