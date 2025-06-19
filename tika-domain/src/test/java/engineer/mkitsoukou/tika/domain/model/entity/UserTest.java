package engineer.mkitsoukou.tika.domain.model.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import engineer.mkitsoukou.tika.domain.exception.InvalidEmailException;
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
    role        = new Role(
      new RoleId(UUID.randomUUID()),
      new RoleName("ADMIN"),
      Set.of()
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
        NullPointerException.class,
        () -> User.register(null, pw, svc)
      );
    }

    @Test
    void registerWithNullPasswordThrows() {
      assertThrows(
        NullPointerException.class,
        () -> User.register(email, null, svc)
      );
    }

    @Test
    void registerWithNullServiceThrows() {
      assertThrows(
        NullPointerException.class,
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
        IllegalArgumentException.class,
        () -> user.changePassword(pw, newPw, svc)
      );
    }

    @Test
    void changePasswordWithNullOldThrows() {
      assertThrows(
        NullPointerException.class,
        () -> user.changePassword(null, newPw, svc)
      );
    }

    @Test
    void changePasswordWithNullNewThrows() {
      assertThrows(
        NullPointerException.class,
        () -> user.changePassword(pw, null, svc)
      );
    }

    @Test
    void changePasswordWithNullServiceThrows() {
      assertThrows(
        NullPointerException.class,
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
        NullPointerException.class,
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
      var other = new Role(
        new RoleId(UUID.randomUUID()),
        new RoleName("OTHER"),
        Set.of()
      );
      assertThrows(
        IllegalArgumentException.class,
        () -> user.removeRole(other)
      );
    }

    @Test
    void removeNullRoleThrows() {
      assertThrows(
        NullPointerException.class,
        () -> user.removeRole(null)
      );
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
