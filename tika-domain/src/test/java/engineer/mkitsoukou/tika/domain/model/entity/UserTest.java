package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.*;
import engineer.mkitsoukou.tika.domain.model.event.*;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import engineer.mkitsoukou.tika.domain.service.PasswordHasher;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity")
class UserTest {

  private static final Instant NOW = Instant.EPOCH;

  private static final class FakePasswordHasher implements PasswordHasher {
    @Override public PasswordHash hash(PlainPassword plain) {
      return new PasswordHash("$2" + plain.clearText());
    }
    @Override public boolean matches(PlainPassword p, PasswordHash h) {
      return h.hash().equals("$2" + p.clearText());
    }
  }
  private final PasswordHasher hasher = new FakePasswordHasher();

  private UserFixtures fixtures;

  @BeforeEach
  void prepareFixtures() {
    fixtures = new UserFixtures(
        new Email("alice@example.com"),
        new PlainPassword("P@ssw0rd!"),
        new PlainPassword("N3wP@ss!"));
  }

  private record UserFixtures(Email email, PlainPassword pwd, PlainPassword newPwd) {}

  private Role role(String name, String perm) {
    return Role.createRole(new RoleName(name), Set.of(new Permission(perm)));
  }

  @Nested @DisplayName("Registration")
  class Registration {
    @Test @DisplayName("register emits UserRegistered & hashes pwd")
    void register_emits() {
      var user = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("$2" + fixtures.pwd.clearText()));
      var events = user.pullEvents();
      assertThat(events).singleElement().isInstanceOf(UserRegistered.class);
    }

    @Test @DisplayName("null args throw (Z)")
    void nullArgs_throw() {
      assertThatThrownBy(() -> User.register(null, fixtures.pwd, hasher, NOW)).isInstanceOf(EntityRequiredFieldException.class);
      assertThatThrownBy(() -> User.register(fixtures.email, null, hasher, NOW)).isInstanceOf(EntityRequiredFieldException.class);
      assertThatThrownBy(() -> User.register(fixtures.email, fixtures.pwd, null, NOW)).isInstanceOf(EntityRequiredFieldException.class);
      assertThatThrownBy(() -> User.register(fixtures.email, fixtures.pwd, hasher, null)).isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  @Nested @DisplayName("Password Management")
  class PasswordManagement {
    private User user;
    @BeforeEach void setUp() {
      user = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      user.pullEvents();
    }

    @Test @DisplayName("changePassword happy path")
    void changePassword_ok() {
      user.changePassword(fixtures.pwd, fixtures.newPwd, hasher, NOW);
      assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("$2" + fixtures.newPwd.clearText()));
      assertThat(user.pullEvents()).singleElement().isInstanceOf(PasswordChanged.class);
    }

    @Test @DisplayName("changePassword wrong old throws")
    void changePassword_wrong() {
      assertThatThrownBy(() -> user.changePassword(new PlainPassword("badPassword1!"), fixtures.newPwd, hasher, NOW))
          .isInstanceOf(IncorrectPasswordException.class);
    }

    @Test @DisplayName("resetPassword emits event")
    void resetPassword_ok() {
      user.resetPassword(fixtures.newPwd, hasher, NOW);
      assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("$2" + fixtures.newPwd.clearText()));
      assertThat(user.pullEvents()).singleElement().isInstanceOf(PasswordChanged.class);
    }
  }

  @Nested @DisplayName("Role Management")
  class RoleManagement {
    private User user;
    private Role admin;
    private Role reader;

    @BeforeEach void setUp() {
      user = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      user.pullEvents();
      admin = role("ADMIN", "admin.access");
      reader = role("READER", "resource.read");
    }

    @Test @DisplayName("assignRole emits RoleAssigned")
    void assignRole_emits() {
      user.assignRole(admin, NOW);
      assertThat(user.hasRole(admin)).isTrue();
      assertThat(user.pullEvents()).singleElement().isInstanceOf(RoleAssigned.class);
    }

    @Test @DisplayName("duplicate assign ignored")
    void duplicateAssign() {
      user.assignRole(admin, NOW);
      user.pullEvents();
      user.assignRole(admin, NOW);
      assertThat(user.getRoles()).hasSize(1);
      assertThat(user.pullEvents()).isEmpty();
    }

    @Test @DisplayName("removeRole emits RoleRemoved")
    void removeRole_ok() {
      user.assignRoles(Set.of(admin, reader), NOW);
      user.pullEvents();
      user.removeRole(admin, NOW);
      assertThat(user.hasRole(admin)).isFalse();
      assertThat(user.pullEvents()).singleElement().isInstanceOf(RoleRemoved.class);
    }

    @Test @DisplayName("remove last role throws")
    void removeLastRole() {
      user.assignRole(admin, NOW);
      user.pullEvents();
      assertThatThrownBy(() -> user.removeRole(admin, NOW)).isInstanceOf(NoRolesAssignedException.class);
    }

    @Test @DisplayName("hasPermission returns true when user has role with permission")
    void hasPermission_userHasRoleWithPermission_returnsTrue() {
      user.assignRole(admin, NOW);
      user.pullEvents();

      var adminPermission = new Permission("admin.access");
      assertThat(user.hasPermission(adminPermission)).isTrue();
    }

    @Test @DisplayName("hasPermission returns false when user lacks permission")
    void hasPermission_userLacksPermission_returnsFalse() {
      user.assignRole(reader, NOW);
      user.pullEvents();

      var adminPermission = new Permission("admin.access");
      assertThat(user.hasPermission(adminPermission)).isFalse();
    }

    @Test @DisplayName("hasPermission returns true when user has any role with permission")
    void hasPermission_userHasMultipleRolesOneWithPermission_returnsTrue() {
      user.assignRoles(Set.of(admin, reader), NOW);
      user.pullEvents();

      var readerPermission = new Permission("resource.read");
      assertThat(user.hasPermission(readerPermission)).isTrue();
    }

    @Test @DisplayName("hasPermission throws when permission is null")
    void hasPermission_nullPermission_throws() {
      user.assignRole(admin, NOW);
      user.pullEvents();

      assertThatThrownBy(() -> user.hasPermission(null))
          .isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test @DisplayName("removeRoles removes multiple roles and emits events")
    void removeRoles_multiplRoles_removesAndEmitsEvents() {
      var moderator = role("MODERATOR", "moderate.content");
      user.assignRoles(Set.of(admin, reader, moderator), NOW);
      user.pullEvents();

      user.removeRoles(Set.of(admin, reader), NOW);

      assertThat(user.hasRole(admin)).isFalse();
      assertThat(user.hasRole(reader)).isFalse();
      assertThat(user.hasRole(moderator)).isTrue();
      assertThat(user.getRoles()).hasSize(1);

      var events = user.pullEvents();
      assertThat(events)
          .hasSize(2)
          .allMatch(RoleRemoved.class::isInstance);
    }

    @Test @DisplayName("removeRoles throws when removing all roles")
    void removeRoles_removingAllRoles_throws() {
      user.assignRoles(Set.of(admin, reader), NOW);
      user.pullEvents();

      var rolesToRemove = Set.of(admin, reader);
      assertThatThrownBy(() -> user.removeRoles(rolesToRemove, NOW))
          .isInstanceOf(NoRolesAssignedException.class);
    }

    @Test @DisplayName("removeRoles throws when removing too many roles")
    void removeRoles_removingTooManyRoles_throws() {
      var moderator = role("MODERATOR", "moderate.content");
      user.assignRoles(Set.of(admin, reader, moderator), NOW);
      user.pullEvents();

      var rolesToRemove = Set.of(admin, reader, moderator);
      assertThatThrownBy(() -> user.removeRoles(rolesToRemove, NOW))
          .isInstanceOf(NoRolesAssignedException.class);
    }

    @Test @DisplayName("removeRoles throws when role not assigned")
    void removeRoles_roleNotAssigned_throws() {
      user.assignRole(admin, NOW);
      user.pullEvents();

      var rolesToRemove = Set.of(reader);
      assertThatThrownBy(() -> user.removeRoles(rolesToRemove, NOW))
          .isInstanceOf(RoleNotFoundException.class);
    }

    @Test @DisplayName("removeRoles throws when roles parameter is null")
    void removeRoles_nullRoles_throws() {
      user.assignRole(admin, NOW);
      user.pullEvents();

      assertThatThrownBy(() -> user.removeRoles(null, NOW))
          .isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test @DisplayName("removeRoles throws when now parameter is null")
    void removeRoles_nullNow_throws() {
      user.assignRoles(Set.of(admin, reader), NOW);
      user.pullEvents();

      var rolesToRemove = Set.of(admin);
      assertThatThrownBy(() -> user.removeRoles(rolesToRemove, null))
          .isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  @Nested @DisplayName("Activation")
  class Activation {
    private User user;
    @BeforeEach void setUp() {
      user = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      user.pullEvents();
    }

    @Test @DisplayName("activate / deactivate emit events")
    void toggle() {
      user.desactivate(NOW);
      var deactEvt = user.pullEvents();
      assertThat(user.isActive()).isFalse();
      assertThat(deactEvt).singleElement().isInstanceOf(UserActivationChanged.class);

      user.activate(NOW);
      var actEvt = user.pullEvents();
      assertThat(user.isActive()).isTrue();
      assertThat(actEvt).singleElement().isInstanceOf(UserActivationChanged.class);
    }
  }

  @Nested @DisplayName("Object Contract")
  class ObjectContract {
    @Test void equalsHashToString() {
      var u1 = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      var u2 = u1;
      var u3 = User.register(fixtures.email, fixtures.pwd, hasher, NOW);
      assertThat(u1).isEqualTo(u2).isNotEqualTo(u3);
      assertThat(u1.hashCode()).isNotEqualTo(u3.hashCode());
      assertThat(u1.toString())
          .contains("User")
          .contains(u1.getId().toString())
          .contains(fixtures.email.toString());
    }
  }
}
