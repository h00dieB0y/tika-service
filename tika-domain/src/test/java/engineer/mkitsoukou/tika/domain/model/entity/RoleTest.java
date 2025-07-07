package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.EmptyRoleException;
import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.exception.PermissionNotFoundException;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role Entity")
class RoleTest {

  private static final Instant NOW = Instant.EPOCH;

  private RoleFixtures fixtures;

  @BeforeEach
  void prepareTestFixtures() {
    fixtures = new RoleFixtures(
        new RoleName("ADMIN_ROLE"),
        new Permission("resource.read"),
        new Permission("resource.write"),
        new Permission("resource.delete"),
        new Permission("resource.update")
    );
  }

  private record RoleFixtures(
      RoleName adminRoleName,
      Permission readPermission,
      Permission writePermission,
      Permission deletePermission,
      Permission updatePermission) {
  }

  // ─────────────────────────── Role creation ────────────────────────────
  @Nested
  @DisplayName("Role Creation")
  class WhenCreatingRole {
    @Test
    @DisplayName("should succeed with valid name and permissions")
    void shouldSucceedWithValidNameAndPermissions() {
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));

      assertThat(role.getRoleId()).isNotNull();
      assertThat(role.getRoleName()).isEqualTo(fixtures.adminRoleName());
      assertThat(role.getPermissions())
          .containsExactly(fixtures.readPermission());
    }

    @Test
    @DisplayName("should reject creation with empty permissions")
    void shouldRejectCreationWithEmptyPermissions() {
      assertThatThrownBy(() -> Role.createRole(fixtures.adminRoleName(), Set.of()))
          .isInstanceOf(EmptyRoleException.class);
    }

    @Test
    @DisplayName("should reject creation with null name")
    void shouldRejectCreationWithNullName() {
      assertThatThrownBy(() -> Role.createRole(null, Set.of(fixtures.readPermission())))
          .isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test
    @DisplayName("should reject creation with null permissions set")
    void shouldRejectCreationWithNullPermissionsSet() {
      assertThatThrownBy(() -> Role.createRole(fixtures.adminRoleName(), null))
          .isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  // ──────────────────────── Permission management ───────────────────────
  @Nested
  @DisplayName("Permission Management")
  class WhenManagingPermissions {
    private Role adminRole;

    @BeforeEach
    void prepareRole() {
      adminRole = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      adminRole.pullEvents(); // clear creation event
    }

    // ────────── Single permission ops ──────────
    @Nested
    @DisplayName("Single permission operations")
    class SinglePermissionOperations {
      @Test @DisplayName("addPermission emits event")
      void addPermission_emits() {
        adminRole.addPermission(fixtures.writePermission(), NOW);

        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();
        var events = adminRole.pullEvents();
        assertThat(events).singleElement().isInstanceOf(PermissionAdded.class);
      }

      @Test @DisplayName("duplicate add ignored")
      void duplicateAdd_ignored() {
        adminRole.addPermission(fixtures.readPermission(), NOW);
        assertThat(adminRole.pullEvents()).isEmpty();
      }

      @Test @DisplayName("removePermission emits event")
      void removePermission_emits() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.pullEvents();
        adminRole.removePermission(fixtures.writePermission(), NOW);

        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();
        assertThat(adminRole.pullEvents()).singleElement().isInstanceOf(PermissionRemoved.class);
      }

      @Test @DisplayName("remove last permission throws EmptyRole")
      void removeLast_throws() {
        assertThatThrownBy(() -> adminRole.removePermission(fixtures.readPermission(), NOW))
            .isInstanceOf(EmptyRoleException.class);
      }
    }

    // ────────── Bulk permission ops ──────────
    @Nested
    @DisplayName("Bulk permission operations")
    class BulkPermissionOperations {
      @Test @DisplayName("bulk add emits events for new perms only")
      void bulkAdd() {
        adminRole.addPermissions(Set.of(fixtures.readPermission(), fixtures.writePermission()), NOW);
        var events = adminRole.pullEvents();
        assertThat(events).singleElement().isInstanceOf(PermissionAdded.class);
      }

      @Test @DisplayName("bulk remove emits events and keeps at least one perm")
      void bulkRemove() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.pullEvents();
        adminRole.removePermissions(Set.of(fixtures.writePermission()), NOW);
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue();
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();
      }

      @Test @DisplayName("bulk remove all throws EmptyRole")
      void bulkRemoveAll_throws() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.pullEvents();
        assertThatThrownBy(() -> adminRole.removePermissions(Set.of(fixtures.readPermission(), fixtures.writePermission()), NOW))
            .isInstanceOf(EmptyRoleException.class);
      }

      @Test @DisplayName("bulk remove unknown perm throws")
      void bulkRemoveUnknown() {
        assertThatThrownBy(() -> adminRole.removePermissions(Set.of(fixtures.deletePermission()), NOW))
            .isInstanceOf(PermissionNotFoundException.class);
      }

      @Test @DisplayName("null set to bulk add/remove throws")
      void nullBulkSet_throws() {
        assertThatThrownBy(() -> adminRole.addPermissions(null, NOW)).isInstanceOf(EntityRequiredFieldException.class);
        assertThatThrownBy(() -> adminRole.removePermissions(null, NOW)).isInstanceOf(EntityRequiredFieldException.class);
      }
    }
  }

  // ───────────────────────────── Role invariants ───────────────────────────
  @Nested
  @DisplayName("Role Invariants")
  class RoleInvariants {
    @Test @DisplayName("permissions collection is unmodifiable")
    void permissionsUnmodifiable() {
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      assertThatThrownBy(() -> role.getPermissions().add(fixtures.writePermission()))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    @DisplayName("Object contract")
    class ObjectContract {
      @Test @DisplayName("equals compares by roleId")
      void equalsById() {
        var r1 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        var r2 = r1;
        var r3 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        assertThat(r1).isEqualTo(r2).isNotEqualTo(r3);
      }

      @Test @DisplayName("consistent hashCode by roleId")
      void hashCodeById() {
        var r1 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        var r2 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        assertThat(r1.hashCode()).isEqualTo(r1.hashCode());
        // high probability different
        assertThat(r1.hashCode()).isNotEqualTo(r2.hashCode());
      }

      @Test @DisplayName("toString contains key info")
      void toStringContains() {
        var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission(), fixtures.writePermission()));
        var str = role.toString();
        assertThat(str)
            .contains("Role")
            .contains(role.getRoleId().toString())
            .contains(fixtures.adminRoleName().toString())
            .contains("permissionsCount=2");
      }
    }
  }
}
