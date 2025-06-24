package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.*;
import engineer.mkitsoukou.tika.domain.model.event.*;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Entity")
class UserTest {

  @Mock
  private PasswordHasher passwordHasher;
  private UserCredentials credentials;
  private UserRoles roles;
  private PasswordHashes hashes;

  @BeforeEach
  void prepareTestFixtures() {
    credentials = new UserCredentials(
        new Email("alice@example.com"),
        new PlainPassword("P@ssw0rd!"),
        new PlainPassword("N3wP@ss!")
    );

    roles = new UserRoles(
        Role.createRole(
            new RoleName("ADMIN"),
            Set.of(new Permission("admin.access"))
        ),
        Role.createRole(
            new RoleName("READER_ROLE"),
            Set.of(new Permission("resource.read"))
        ),
        Role.createRole(
            new RoleName("WRITER_ROLE"),
            Set.of(new Permission("resource.write"))
        )
    );

    hashes = new PasswordHashes(
        new PasswordHash("$2a$10$eImiTMZG4T5x1j6d8f9eOe1z5b3Z5h5k5f5k5f5k5f5k5f5k5f5k"),
        new PasswordHash("$2a$11$eImiTMZG4T5x1j6d8f9eOe1z5b3Z5h5k5f5k5f5k5f5k5f5k5f5k")
    );
  }

  @Test
  @DisplayName("should clear events after they are pulled")
  void shouldClearEventsAfterTheyArePulled() {
    // Arrange
    when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
    when(passwordHasher.hash(credentials.newPassword())).thenReturn(hashes.updated());
    when(passwordHasher.match(credentials.password(), hashes.initial())).thenReturn(true);

    var user = User.register(credentials.email(), credentials.password(), passwordHasher);
    user.changePassword(credentials.password(), credentials.newPassword(), passwordHasher);
    user.assignRole(roles.adminRole());

    // Act
    var firstPull = user.pullEvents();
    var secondPull = user.pullEvents();

    // Assert
    assertThat(firstPull).hasSize(3);
    assertThat(secondPull).isEmpty();
  }

  // Test fixtures as records for immutability
  private record UserCredentials(Email email, PlainPassword password, PlainPassword newPassword) {
  }

  private record UserRoles(Role adminRole, Role readerRole, Role writerRole) {
  }

  private record PasswordHashes(PasswordHash initial, PasswordHash updated) {
  }

  @Nested
  @DisplayName("User Registration")
  class WhenRegisteringUser {
    @Test
    @DisplayName("should create user and emit UserRegistered event")
    void shouldCreateUserAndEmitRegisteredEvent() {
      // Arrange
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());

      // Act
      var user = User.register(credentials.email(), credentials.password(), passwordHasher);

      // Assert properties
      assertThat(user.getId()).isNotNull();
      assertThat(user.getEmail()).isEqualTo(credentials.email());
      assertThat(user.getPasswordHash()).isEqualTo(hashes.initial());

      // Assert event
      var events = user.pullEvents();
      assertThat(events).hasSize(1);

      assertThat(events.getFirst())
          .isInstanceOf(UserRegistered.class)
          .extracting(UserRegistered.class::cast)
          .returns(user.getId(), UserRegistered::getUserId)
          .returns(credentials.email(), UserRegistered::getEmail);
    }

    @Test
    @DisplayName("should reject registration with null email")
    void shouldRejectRegistrationWithNullEmail() {
      assertThatThrownBy(() ->
          User.register(null, credentials.password(), passwordHasher)
      ).isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test
    @DisplayName("should reject registration with null password")
    void shouldRejectRegistrationWithNullPassword() {
      assertThatThrownBy(() ->
          User.register(credentials.email(), null, passwordHasher)
      ).isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test
    @DisplayName("should reject registration with null password service")
    void shouldRejectRegistrationWithNullPasswordService() {
      assertThatThrownBy(() ->
          User.register(credentials.email(), credentials.password(), null)
      ).isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test
    @DisplayName("should reject registration with invalid email format")
    void shouldRejectRegistrationWithInvalidEmailFormat() {
      assertThatThrownBy(() ->
          User.register(new Email("not-an-email"), credentials.password(), passwordHasher)
      ).isInstanceOf(InvalidEmailException.class);
    }
  }

  @Nested
  @DisplayName("Password Management")
  class WhenManagingPassword {
    private User authenticatedUser;

    @BeforeEach
    void prepareAuthenticatedUser() {
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      authenticatedUser = User.register(credentials.email(), credentials.password(), passwordHasher);
      authenticatedUser.pullEvents();  // clear the registration event
    }

    @Nested
    @DisplayName("When changing password")
    class PasswordChange {
      @Test
      @DisplayName("should accept valid current password and emit PasswordChanged event")
      void shouldAcceptValidCurrentPasswordAndEmitEvent() {
        // Arrange
        when(passwordHasher.match(credentials.password(), hashes.initial())).thenReturn(true);
        when(passwordHasher.hash(credentials.newPassword())).thenReturn(hashes.updated());
        var userId = authenticatedUser.getId();

        // Act
        authenticatedUser.changePassword(
            credentials.password(),
            credentials.newPassword(),
            passwordHasher
        );

        // Assert password updated
        assertThat(authenticatedUser.getPasswordHash()).isEqualTo(hashes.updated());

        // Assert event
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(PasswordChanged.class)
            .extracting(PasswordChanged.class::cast)
            .returns(userId, PasswordChanged::getUserId);
      }

      @Test
      @DisplayName("should reject incorrect current password")
      void shouldRejectIncorrectCurrentPassword() {
        // Arrange
        when(passwordHasher.match(credentials.password(), hashes.initial())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() ->
            authenticatedUser.changePassword(
                credentials.password(),
                credentials.newPassword(),
                passwordHasher
            )
        ).isInstanceOf(IncorrectPasswordException.class);
      }

      @Test
      @DisplayName("should reject null current password")
      void shouldRejectNullCurrentPassword() {
        assertThatThrownBy(() ->
            authenticatedUser.changePassword(null, credentials.newPassword(), passwordHasher)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should reject null new password")
      void shouldRejectNullNewPassword() {
        assertThatThrownBy(() ->
            authenticatedUser.changePassword(credentials.password(), null, passwordHasher)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should reject null password service")
      void shouldRejectNullPasswordService() {
        assertThatThrownBy(() ->
            authenticatedUser.changePassword(credentials.password(), credentials.newPassword(), null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }
    }

    @Nested
    @DisplayName("When resetting password")
    class PasswordReset {
      @Test
      @DisplayName("should reset password and emit PasswordChanged event")
      void shouldResetPasswordAndEmitEvent() {
        // Arrange
        when(passwordHasher.hash(credentials.newPassword())).thenReturn(hashes.updated());
        var userId = authenticatedUser.getId();

        // Act
        authenticatedUser.resetPassword(credentials.newPassword(), passwordHasher);

        // Assert password updated
        assertThat(authenticatedUser.getPasswordHash()).isEqualTo(hashes.updated());

        // Assert event
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(PasswordChanged.class)
            .extracting(PasswordChanged.class::cast)
            .returns(userId, PasswordChanged::getUserId);
      }

      @Test
      @DisplayName("should reject null password")
      void shouldRejectNullPassword() {
        assertThatThrownBy(() ->
            authenticatedUser.resetPassword(null, passwordHasher)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should reject null password service")
      void shouldRejectNullPasswordService() {
        assertThatThrownBy(() ->
            authenticatedUser.resetPassword(credentials.newPassword(), null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }
    }
  }

  @Nested
  @DisplayName("Role Management")
  class WhenManagingRoles {
    private User authenticatedUser;

    @BeforeEach
    void prepareUserWithoutRoles() {
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      authenticatedUser = User.register(credentials.email(), credentials.password(), passwordHasher);
      authenticatedUser.pullEvents();  // clear the registration event
    }

    @Nested
    @DisplayName("Single role operations")
    class SingleRoleOperations {
      @Test
      @DisplayName("should assign role and emit RoleAssigned event")
      void shouldAssignRoleAndEmitEvent() {
        // Act
        var userId = authenticatedUser.getId();
        authenticatedUser.assignRole(roles.adminRole());

        // Assert role added
        assertThat(authenticatedUser.getRoles()).contains(roles.adminRole());

        // Assert event
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(RoleAssigned.class)
            .extracting(RoleAssigned.class::cast)
            .returns(userId, RoleAssigned::getUserId)
            .returns(roles.adminRole().getRoleId(), RoleAssigned::getRoleId);
      }

      @Test
      @DisplayName("should ignore duplicate role assignment")
      void shouldIgnoreDuplicateRoleAssignment() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act
        authenticatedUser.assignRole(roles.adminRole());

        // Assert
        assertThat(authenticatedUser.getRoles()).hasSize(1);
        assertThat(authenticatedUser.pullEvents()).isEmpty();
      }

      @Test
      @DisplayName("should reject null role during assignment")
      void shouldRejectNullRoleDuringAssignment() {
        assertThatThrownBy(() ->
            authenticatedUser.assignRole(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should remove role and emit RoleRemoved event")
      void shouldRemoveRoleAndEmitEvent() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.assignRole(roles.readerRole());
        authenticatedUser.pullEvents();

        var userId = authenticatedUser.getId();

        // Act
        authenticatedUser.removeRole(roles.adminRole());

        // Assert role removed
        assertThat(authenticatedUser.getRoles()).doesNotContain(roles.adminRole());
        assertThat(authenticatedUser.getRoles()).contains(roles.readerRole());

        // Assert event
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(RoleRemoved.class)
            .extracting(RoleRemoved.class::cast)
            .returns(userId, RoleRemoved::getUserId)
            .returns(roles.adminRole().getRoleId(), RoleRemoved::getRoleId);
      }

      @Test
      @DisplayName("should reject removing non-existent role")
      void shouldRejectRemovingNonexistentRole() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThatThrownBy(() ->
            authenticatedUser.removeRole(roles.readerRole())
        ).isInstanceOf(RoleNotFoundException.class);
      }

      @Test
      @DisplayName("should reject null role during removal")
      void shouldRejectNullRoleDuringRemoval() {
        assertThatThrownBy(() ->
            authenticatedUser.removeRole(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should not allow removing last role")
      void shouldNotAllowRemovingLastRole() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThatThrownBy(() ->
            authenticatedUser.removeRole(roles.adminRole())
        ).isInstanceOf(NoRolesAssignedException.class);
      }
    }

    @Nested
    @DisplayName("Bulk role operations")
    class BulkRoleOperations {
      @Test
      @DisplayName("should assign multiple roles at once")
      void shouldAssignMultipleRolesAtOnce() {
        // Act
        authenticatedUser.assignRoles(Set.of(
            roles.adminRole(),
            roles.readerRole(),
            roles.writerRole()
        ));

        // Assert roles added
        assertThat(authenticatedUser.getRoles())
            .hasSize(3)
            .contains(roles.adminRole(), roles.readerRole(), roles.writerRole());

        // Assert events
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(3)
            .allMatch(RoleAssigned.class::isInstance);
      }

      @Test
      @DisplayName("should reject null set during bulk assignment")
      void shouldRejectNullSetDuringBulkAssignment() {
        assertThatThrownBy(() ->
            authenticatedUser.assignRoles(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should do nothing with empty set during bulk assignment")
      void shouldDoNothingWithEmptySetDuringBulkAssignment() {
        // Act
        authenticatedUser.assignRoles(Set.of());

        // Assert
        assertThat(authenticatedUser.getRoles()).isEmpty();
        assertThat(authenticatedUser.pullEvents()).isEmpty();
      }

      @Test
      @DisplayName("should remove multiple roles at once")
      void shouldRemoveMultipleRolesAtOnce() {
        // Arrange
        authenticatedUser.assignRoles(Set.of(
            roles.adminRole(),
            roles.readerRole(),
            roles.writerRole()
        ));
        authenticatedUser.pullEvents();

        // Act
        authenticatedUser.removeRoles(Set.of(roles.adminRole(), roles.readerRole()));

        // Assert roles removed
        assertThat(authenticatedUser.getRoles())
            .hasSize(1)
            .contains(roles.writerRole())
            .doesNotContain(roles.adminRole(), roles.readerRole());

        // Assert events
        var events = authenticatedUser.pullEvents();
        assertThat(events).hasSize(2)
            .allMatch(RoleRemoved.class::isInstance);
      }

      @Test
      @DisplayName("should reject null set during bulk removal")
      void shouldRejectNullSetDuringBulkRemoval() {
        assertThatThrownBy(() ->
            authenticatedUser.removeRoles(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should do nothing with empty set during bulk removal")
      void shouldDoNothingWithEmptySetDuringBulkRemoval() {
        // Arrange
        authenticatedUser.assignRoles(Set.of(roles.adminRole(), roles.readerRole()));
        authenticatedUser.pullEvents();

        // Act
        authenticatedUser.removeRoles(Set.of());

        // Assert
        assertThat(authenticatedUser.getRoles()).hasSize(2);
        assertThat(authenticatedUser.pullEvents()).isEmpty();
      }

      @Test
      @DisplayName("should reject removing non-existent roles in bulk")
      void shouldRejectRemovingNonexistentRolesInBulk() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThatThrownBy(() ->
            authenticatedUser.removeRoles(Set.of(roles.readerRole()))
        ).isInstanceOf(RoleNotFoundException.class);
      }

      @Test
      @DisplayName("should not allow removing all roles")
      void shouldNotAllowRemovingAllRoles() {
        // Arrange
        authenticatedUser.assignRoles(Set.of(roles.adminRole(), roles.readerRole()));
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThatThrownBy(() ->
            authenticatedUser.removeRoles(Set.of(roles.adminRole(), roles.readerRole()))
        ).isInstanceOf(NoRolesAssignedException.class);
      }
    }

    @Nested
    @DisplayName("Role membership verification")
    class RoleMembershipVerification {
      @Test
      @DisplayName("should confirm membership for assigned role")
      void shouldConfirmMembershipForAssignedRole() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThat(authenticatedUser.hasRole(roles.adminRole())).isTrue();
      }

      @Test
      @DisplayName("should deny membership for unassigned role")
      void shouldDenyMembershipForUnassignedRole() {
        // Arrange
        authenticatedUser.assignRole(roles.adminRole());
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThat(authenticatedUser.hasRole(roles.readerRole())).isFalse();
      }

      @Test
      @DisplayName("should reject null role during membership check")
      void shouldRejectNullRoleDuringMembershipCheck() {
        assertThatThrownBy(() ->
            authenticatedUser.hasRole(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should distinguish between roles with same name but different permissions")
      void shouldDistinguishBetweenRolesWithSameNameButDifferentPermissions() {
        // Arrange
        var name = new RoleName("DUPLICATE_NAME");
        var roleA = Role.createRole(name, Set.of(new Permission("a.permission")));
        var roleB = Role.createRole(name, Set.of(new Permission("b.permission")));

        authenticatedUser.assignRole(roleA);
        authenticatedUser.pullEvents();

        // Act & Assert
        assertThat(authenticatedUser.hasRole(roleA)).isTrue();
        assertThat(authenticatedUser.hasRole(roleB)).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("Permission Verification")
  class WhenVerifyingPermissions {
    private User authenticatedUser;
    private Permission readPermission;
    private Permission writePermission;

    @BeforeEach
    void prepareUserWithoutPermissions() {
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      authenticatedUser = User.register(credentials.email(), credentials.password(), passwordHasher);
      authenticatedUser.pullEvents();

      readPermission = new Permission("resource.read");
      writePermission = new Permission("resource.write");
    }

    @Test
    @DisplayName("should have no permissions when no roles assigned")
    void shouldHaveNoPermissionsWhenNoRolesAssigned() {
      assertThat(authenticatedUser.hasPermission(readPermission)).isFalse();
      assertThat(authenticatedUser.hasPermission(writePermission)).isFalse();
    }

    @Test
    @DisplayName("should have permissions from assigned roles")
    void shouldHavePermissionsFromAssignedRoles() {
      // Arrange & Act
      authenticatedUser.assignRole(roles.readerRole());

      // Assert
      assertThat(authenticatedUser.hasPermission(readPermission)).isTrue();
      assertThat(authenticatedUser.hasPermission(writePermission)).isFalse();

      // Assign additional role
      authenticatedUser.assignRole(roles.writerRole());

      // Assert both permissions now available
      assertThat(authenticatedUser.hasPermission(readPermission)).isTrue();
      assertThat(authenticatedUser.hasPermission(writePermission)).isTrue();
    }

    @Test
    @DisplayName("should lose permissions when role removed")
    void shouldLosePermissionsWhenRoleRemoved() {
      // Arrange
      authenticatedUser.assignRole(roles.readerRole());
      authenticatedUser.assignRole(roles.writerRole());

      // Act
      authenticatedUser.removeRole(roles.readerRole());

      // Assert
      assertThat(authenticatedUser.hasPermission(readPermission)).isFalse();
      assertThat(authenticatedUser.hasPermission(writePermission)).isTrue();
    }

    @Test
    @DisplayName("should reject null permission during permission check")
    void shouldRejectNullPermissionDuringPermissionCheck() {
      assertThatThrownBy(() ->
          authenticatedUser.hasPermission(null)
      ).isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  @Nested
  @DisplayName("Account Activation")
  class WhenManagingAccountActivation {
    private User authenticatedUser;

    @BeforeEach
    void prepareActiveUser() {
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      authenticatedUser = User.register(credentials.email(), credentials.password(), passwordHasher);
      authenticatedUser.pullEvents();  // clear the registration event
    }

    @Test
    @DisplayName("should be active by default after registration")
    void shouldBeActiveByDefaultAfterRegistration() {
      assertThat(authenticatedUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("should deactivate account and emit UserActivationChanged event")
    void shouldDeactivateAccountAndEmitEvent() {
      // Arrange
      var userId = authenticatedUser.getId();

      // Act
      authenticatedUser.deactivate();

      // Assert account deactivated
      assertThat(authenticatedUser.isActive()).isFalse();

      // Assert event
      var events = authenticatedUser.pullEvents();
      assertThat(events).hasSize(1);

      assertThat(events.getFirst())
          .isInstanceOf(UserActivationChanged.class)
          .extracting(UserActivationChanged.class::cast)
          .returns(userId, UserActivationChanged::getUserId)
          .returns(false, UserActivationChanged::isActive);
    }

    @Test
    @DisplayName("should reactivate account and emit UserActivationChanged event")
    void shouldReactivateAccountAndEmitEvent() {
      // Arrange
      authenticatedUser.deactivate();
      authenticatedUser.pullEvents();
      var userId = authenticatedUser.getId();

      // Act
      authenticatedUser.activate();

      // Assert account activated
      assertThat(authenticatedUser.isActive()).isTrue();

      // Assert event
      var events = authenticatedUser.pullEvents();
      assertThat(events).hasSize(1);

      assertThat(events.getFirst())
          .isInstanceOf(UserActivationChanged.class)
          .extracting(UserActivationChanged.class::cast)
          .returns(userId, UserActivationChanged::getUserId)
          .returns(true, UserActivationChanged::isActive);
    }

    @Test
    @DisplayName("should emit event when activating already active account")
    void shouldEmitEventWhenActivatingAlreadyActiveAccount() {
      // Arrange
      var userId = authenticatedUser.getId();

      // Act
      authenticatedUser.activate();

      // Assert account still active
      assertThat(authenticatedUser.isActive()).isTrue();

      // Assert event still emitted
      var events = authenticatedUser.pullEvents();
      assertThat(events).hasSize(1);

      assertThat(events.getFirst())
          .isInstanceOf(UserActivationChanged.class)
          .extracting(UserActivationChanged.class::cast)
          .returns(userId, UserActivationChanged::getUserId)
          .returns(true, UserActivationChanged::isActive);
    }

    @Test
    @DisplayName("should emit event when deactivating already inactive account")
    void shouldEmitEventWhenDeactivatingAlreadyInactiveAccount() {
      // Arrange
      authenticatedUser.deactivate();
      authenticatedUser.pullEvents();
      var userId = authenticatedUser.getId();

      // Act
      authenticatedUser.deactivate();

      // Assert account still inactive
      assertThat(authenticatedUser.isActive()).isFalse();

      // Assert event still emitted
      var events = authenticatedUser.pullEvents();
      assertThat(events).hasSize(1);

      assertThat(events.getFirst())
          .isInstanceOf(UserActivationChanged.class)
          .extracting(UserActivationChanged.class::cast)
          .returns(userId, UserActivationChanged::getUserId)
          .returns(false, UserActivationChanged::isActive);
    }
  }

  @Nested
  @DisplayName("Object Contract")
  class ObjectContractTest {
    @Test
    @DisplayName("should compare users based only on userId")
    void shouldCompareUsersBasedOnlyOnUserId() {
      // Arrange - Create users with same emails but different IDs
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      var user1 = User.register(credentials.email(), credentials.password(), passwordHasher);
      var user2 = User.register(credentials.email(), credentials.password(), passwordHasher);

      assertThat(user1)
          .isNotEqualTo(user2)
          .isEqualTo(user1)
          .isNotNull()
          .isNotEqualTo(new Object())
          .isNotEqualTo("Not a User");
    }

    @Test
    @DisplayName("should have consistent hashCode based on userId")
    void shouldHaveConsistentHashCodeBasedOnUserId() {
      // Arrange
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      var user1 = User.register(credentials.email(), credentials.password(), passwordHasher);
      var user1Copy = user1; // Same reference should have same hashCode

      // Act & Assert
      assertThat(user1).hasSameHashCodeAs(user1Copy);

      // Different users should have different hashCodes (high probability)
      var user2 = User.register(credentials.email(), credentials.password(), passwordHasher);
      assertThat(user1)
          .isNotEqualTo(user2)
          .doesNotHaveSameHashCodeAs(user2);
    }

    @Test
    @DisplayName("should provide a descriptive toString representation")
    void shouldProvideDescriptiveToStringRepresentation() {
      // Arrange
      when(passwordHasher.hash(credentials.password())).thenReturn(hashes.initial());
      var user = User.register(credentials.email(), credentials.password(), passwordHasher);
      user.assignRole(roles.adminRole());

      // Act
      var stringRepresentation = user.toString();

      // Assert
      assertThat(stringRepresentation)
          .contains("User")
          .contains(user.getId().toString())
          .contains(credentials.email().toString())
          .contains("roles=1"); // Should mention the count of roles
    }
  }
}
