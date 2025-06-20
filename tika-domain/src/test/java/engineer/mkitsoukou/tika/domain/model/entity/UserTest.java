package engineer.mkitsoukou.tika.domain.model.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.exception.IncorrectPasswordException;
import engineer.mkitsoukou.tika.domain.exception.InvalidEmailException;
import engineer.mkitsoukou.tika.domain.exception.NoRolesAssignedException;
import engineer.mkitsoukou.tika.domain.exception.RoleNotFoundException;
import engineer.mkitsoukou.tika.domain.model.event.*;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.service.PasswordService;

@ExtendWith(MockitoExtension.class)
class UserTest {

  @Mock
  private PasswordService svc;

  private Email email;
  private PlainPassword pw;
  private PlainPassword newPw;
  private Role role;
  private PasswordHash initialHash;
  private PasswordHash newHash;

  @BeforeEach
  void setUp() {
    email       = new Email("alice@example.com");
    pw          = new PlainPassword("P@ssw0rd!");
    newPw       = new PlainPassword("N3wP@ss!");
    role        = Role.createRole(
      new RoleName("ADMIN"),
      Set.of(new Permission("admin.access"))
    );
    initialHash = new PasswordHash("$2a$10$eImiTMZG4T5x1j6d8f9eOe1z5b3Z5h5k5f5k5f5k5f5k5f5k5f5k");
    newHash     = new PasswordHash("$2a$11$eImiTMZG4T5x1j6d8f9eOe1z5b3Z5h5k5f5k5f5k5f5k5f5k5f5k");
  }

  @Nested
  class RegistrationTests {
    @Test
    void successfulRegistrationEmitsUserRegisteredEvent() {
      // arrange
      when(svc.hash(pw)).thenReturn(initialHash);

      // act
      var user = User.register(email, pw, svc);

      // assert properties
      assertNotNull(user.getId());
      assertEquals(email, user.getEmail());
      assertEquals(initialHash, user.getPasswordHash());

      // assert event
      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var reg = assertInstanceOf(UserRegistered.class, ev.getFirst());
      assertEquals(user.getId(), reg.getUserId());
      assertEquals(email,         reg.getEmail());
    }

    @Test
    void registerWithNullEmailThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> User.register(null, pw, svc)
      );
    }

    @Test
    void registerWithNullPasswordThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> User.register(email, null, svc)
      );
    }

    @Test
    void registerWithNullServiceThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> User.register(email, pw, null)
      );
    }

    @Test
    void registerWithInvalidEmailThrows() {
      assertThrows(
        InvalidEmailException.class,
        () -> User.register(new Email("not-an-email"), pw, svc)
      );
    }
  }

  @Nested
  class ChangePasswordTests {
    private User user;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();  // clear the registration event
    }

    @Test
    void correctOldPasswordEmitsPasswordChangedEvent() {
      // arrange
      when(svc.match(pw, initialHash)).thenReturn(true);
      when(svc.hash(newPw)).thenReturn(newHash);
      var id = user.getId();

      // act
      user.changePassword(pw, newPw, svc);

      // assert new hash
      assertEquals(newHash, user.getPasswordHash());

      // assert event
      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var pc = assertInstanceOf(PasswordChanged.class, ev.getFirst());
      assertEquals(id, pc.getUserId());
    }

    @Test
    void wrongOldPasswordThrows() {
      when(svc.match(pw, initialHash)).thenReturn(false);

      assertThrows(
        IncorrectPasswordException.class,
        () -> user.changePassword(pw, newPw, svc)
      );
    }

    @Test
    void changePasswordWithNullOldThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.changePassword(null, newPw, svc)
      );
    }

    @Test
    void changePasswordWithNullNewThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.changePassword(pw, null, svc)
      );
    }

    @Test
    void changePasswordWithNullServiceThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.changePassword(pw, newPw, null)
      );
    }
  }

  @Nested
  class RoleAssignmentTests {
    private User user;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();
    }

    @Test
    void assignRoleEmitsRoleAssignedEvent() {
      var id = user.getId();

      user.assignRole(role);
      assertTrue(user.getRoles().contains(role));

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var ra = assertInstanceOf(RoleAssigned.class, ev.getFirst());
      assertEquals(id,            ra.getUserId());
      assertEquals(role.getRoleId(), ra.getRoleId());
    }

    @Test
    void assigningSameRoleTwiceIsNoOp() {
      user.assignRole(role);
      user.pullEvents();
      user.assignRole(role);

      assertEquals(1, user.getRoles().size());
      assertTrue(user.pullEvents().isEmpty());
    }

    @Test
    void assignNullRoleThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.assignRole(null)
      );
    }
  }

  @Nested
  class RoleRemovalTests {
    private User user;

    @BeforeEach
    void initUserWithRole() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();
      user.assignRole(role);

      // Add a second role to allow removal of the first one
      var secondRole = Role.createRole(
        new RoleName("SECOND_ROLE"),
        Set.of(new Permission("second.access"))
      );
      user.assignRole(secondRole);

      user.pullEvents();
    }

    @Test
    void removeAssignedRoleEmitsRoleRemovedEvent() {
      var id = user.getId();

      user.removeRole(role);
      assertFalse(user.getRoles().contains(role));

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var rr = assertInstanceOf(RoleRemoved.class, ev.getFirst());
      assertEquals(id,           rr.getUserId());
      assertEquals(role.getRoleId(), rr.getRoleId());
    }

    @Test
    void removingUnassignedRoleThrows() {
      var other = Role.createRole(
        new RoleName("OTHER"),
        Set.of(new Permission("other.access"))
      );
      assertThrows(
        RoleNotFoundException.class,
        () -> user.removeRole(other)
      );
    }

    @Test
    void removeNullRoleThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.removeRole(null)
      );
    }
  }

  @Nested
  class PermissionTests {
    private User user;
    private Permission readPermission;
    private Permission writePermission;
    private Role readerRole;
    private Role writerRole;

    @BeforeEach
    void initUserWithRoles() {
      when(svc.hash(pw)).thenReturn(initialHash);
      readPermission = new Permission("resource.read");
      writePermission = new Permission("resource.write");

      readerRole = Role.createRole(
        new RoleName("READER_ROLE"),
        Set.of(readPermission)
      );

      writerRole = Role.createRole(
        new RoleName("WRITER_ROLE"),
        Set.of(writePermission)
      );

      user = User.register(email, pw, svc);
      user.pullEvents(); // clear registration event
    }

    @Test
    void userWithNoRolesHasNoPermissions() {
      assertFalse(user.hasPermission(readPermission));
      assertFalse(user.hasPermission(writePermission));
    }

    @Test
    void userHasPermissionsFromAssignedRoles() {
      user.assignRole(readerRole);
      assertTrue(user.hasPermission(readPermission));
      assertFalse(user.hasPermission(writePermission));

      user.assignRole(writerRole);
      assertTrue(user.hasPermission(readPermission));
      assertTrue(user.hasPermission(writePermission));
    }

    @Test
    void userLosesPermissionsWhenRoleRemoved() {
      user.assignRole(readerRole);
      user.assignRole(writerRole);

      user.removeRole(readerRole);
      assertFalse(user.hasPermission(readPermission));
      assertTrue(user.hasPermission(writePermission));
    }

    @Test
    void hasPermissionWithNullThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.hasPermission(null)
      );
    }
  }

  @Nested
  class ResetPasswordTests {
    private User user;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();  // clear the registration event
    }

    @Test
    void resetPasswordEmitsPasswordChangedEvent() {
      // arrange
      when(svc.hash(newPw)).thenReturn(newHash);
      var id = user.getId();

      // act
      user.resetPassword(newPw, svc);

      // assert new hash
      assertEquals(newHash, user.getPasswordHash());

      // assert event
      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var pc = assertInstanceOf(PasswordChanged.class, ev.getFirst());
      assertEquals(id, pc.getUserId());
    }

    @Test
    void resetPasswordWithNullPasswordThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.resetPassword(null, svc)
      );
    }

    @Test
    void resetPasswordWithNullServiceThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.resetPassword(newPw, null)
      );
    }
  }

  @Nested
  class ActivationTests {
    private User user;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();  // clear the registration event
    }

    @Test
    void userIsActiveByDefault() {
      assertTrue(user.isActive());
    }

    @Test
    void deactivateEmitsUserActivationChangedEvent() {
      var id = user.getId();

      user.deactivate();

      assertFalse(user.isActive());

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var ac = assertInstanceOf(UserActivationChanged.class, ev.getFirst());
      assertEquals(id, ac.getUserId());
      assertFalse(ac.isActive());
    }

    @Test
    void activateEmitsUserActivationChangedEvent() {
      var id = user.getId();

      // First deactivate
      user.deactivate();
      user.pullEvents();

      // Then reactivate
      user.activate();

      assertTrue(user.isActive());

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var ac = assertInstanceOf(UserActivationChanged.class, ev.getFirst());
      assertEquals(id, ac.getUserId());
      assertTrue(ac.isActive());
    }

    @Test
    void activatingAlreadyActiveUserStillEmitsEvent() {
      var id = user.getId();

      // User is already active by default
      user.activate();

      assertTrue(user.isActive());

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var ac = assertInstanceOf(UserActivationChanged.class, ev.getFirst());
      assertEquals(id, ac.getUserId());
      assertTrue(ac.isActive());
    }

    @Test
    void deactivatingAlreadyInactiveUserStillEmitsEvent() {
      var id = user.getId();

      // First deactivate
      user.deactivate();
      user.pullEvents();

      // Deactivate again
      user.deactivate();

      assertFalse(user.isActive());

      var ev = user.pullEvents();
      assertEquals(1, ev.size());
      var ac = assertInstanceOf(UserActivationChanged.class, ev.getFirst());
      assertEquals(id, ac.getUserId());
      assertFalse(ac.isActive());
    }
  }

  @Nested
  class BulkRoleOperationsTests {
    private User user;
    private Role secondRole;
    private Role thirdRole;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();

      secondRole = Role.createRole(
        new RoleName("SECOND_ROLE"),
        Set.of(new Permission("second.access"))
      );

      thirdRole = Role.createRole(
        new RoleName("THIRD_ROLE"),
        Set.of(new Permission("third.access"))
      );
    }

    @Test
    void assignRolesAddsMultipleRoles() {
      user.assignRoles(Set.of(role, secondRole, thirdRole));

      var roles = user.getRoles();
      assertEquals(3, roles.size());
      assertTrue(roles.contains(role));
      assertTrue(roles.contains(secondRole));
      assertTrue(roles.contains(thirdRole));

      var events = user.pullEvents();
      assertEquals(3, events.size());
      events.forEach(event -> assertInstanceOf(RoleAssigned.class, event));
    }

    @Test
    void assignRolesWithNullSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.assignRoles(null)
      );
    }

    @Test
    void assignRolesWithEmptySetDoesNothing() {
      user.assignRoles(Set.of());
      assertTrue(user.getRoles().isEmpty());
      assertTrue(user.pullEvents().isEmpty());
    }

    @Test
    void removeRolesRemovesMultipleRoles() {
      // First assign roles
      user.assignRoles(Set.of(role, secondRole, thirdRole));
      user.pullEvents();

      // Then remove some
      user.removeRoles(Set.of(role, secondRole));

      var roles = user.getRoles();
      assertEquals(1, roles.size());
      assertTrue(roles.contains(thirdRole));

      var events = user.pullEvents();
      assertEquals(2, events.size());
      events.forEach(event -> assertInstanceOf(RoleRemoved.class, event));
    }

    @Test
    void removeRolesWithNullSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.removeRoles(null)
      );
    }

    @Test
    void removeRolesWithEmptySetDoesNothing() {
      // First assign roles
      user.assignRoles(Set.of(role, secondRole));
      user.pullEvents();

      // Then remove empty set
      user.removeRoles(Set.of());

      assertEquals(2, user.getRoles().size());
      assertTrue(user.pullEvents().isEmpty());
    }

    @Test
    void removeRolesWithNonExistingRoleThrows() {
      // Assign only one role
      user.assignRole(role);
      user.pullEvents();

      // Try to remove a role that doesn't exist
      assertThrows(
        RoleNotFoundException.class,
        () -> user.removeRoles(Set.of(secondRole))
      );
    }

    @Test
    void removeAllRolesThrows() {
      // Assign roles
      user.assignRoles(Set.of(role, secondRole));
      user.pullEvents();

      // Try to remove all roles
      assertThrows(
        NoRolesAssignedException.class,
        () -> user.removeRoles(Set.of(role, secondRole))
      );
    }
  }

  @Nested
  class NoRolesAssignedTests {
    private User user;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();

      // Add one role
      user.assignRole(role);
      user.pullEvents();
    }

    @Test
    void cannotRemoveSingleRole() {
      assertThrows(
        NoRolesAssignedException.class,
        () -> user.removeRole(role)
      );
    }
  }

  @Nested
  class HasRoleTests {
    private User user;
    private Role secondRole;

    @BeforeEach
    void initUser() {
      when(svc.hash(pw)).thenReturn(initialHash);
      user = User.register(email, pw, svc);
      user.pullEvents();

      user.assignRole(role);
      user.pullEvents();

      secondRole = Role.createRole(
        new RoleName("SECOND_ROLE"),
        Set.of(new Permission("second.access"))
      );
    }

    @Test
    void hasRoleReturnsTrueForAssignedRole() {
      assertTrue(user.hasRole(role));
    }

    @Test
    void hasRoleReturnsFalseForUnassignedRole() {
      assertFalse(user.hasRole(secondRole));
    }

    @Test
    void hasRoleWithNullThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> user.hasRole(null)
      );
    }

    @Test
    void hasRoleStillWorksThroughRoleEdgeCase() {
      // Create two roles with the same name but different permissions
      var name = new RoleName("DUPLICATE_NAME");
      var roleA = Role.createRole(name, Set.of(new Permission("a.permission")));
      var roleB = Role.createRole(name, Set.of(new Permission("b.permission")));

      // Add only roleA
      user.assignRole(roleA);
      user.pullEvents();

      // Should find roleA but not roleB
      assertTrue(user.hasRole(roleA));
      assertFalse(user.hasRole(roleB));
    }
  }

  @Test
  void pullEventsClearsBuffer() {
    when(svc.hash(pw)).thenReturn(initialHash);
    when(svc.hash(newPw)).thenReturn(newHash);
    when(svc.match(pw, initialHash)).thenReturn(true);

    var user = User.register(email, pw, svc);
    user.changePassword(pw, newPw, svc);
    user.assignRole(role);

    var first  = user.pullEvents();
    var second = user.pullEvents();
    assertEquals(3, first.size());
    assertTrue(second.isEmpty());
  }
}
