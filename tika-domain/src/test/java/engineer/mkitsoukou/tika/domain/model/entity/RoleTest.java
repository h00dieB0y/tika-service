package engineer.mkitsoukou.tika.domain.model.entity;

import engineer.mkitsoukou.tika.domain.exception.*;
import engineer.mkitsoukou.tika.domain.model.event.*;
import engineer.mkitsoukou.tika.domain.model.valueobject.*;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role Entity")
class RoleTest {

  private static final Instant NOW = Instant.EPOCH;

  private RoleFixtures fixtures;

  @BeforeEach
  void prepareFixtures() {
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
      Permission updatePermission) {}

  // ───────────────────────── Role creation ─────────────────────────
  @Nested @DisplayName("When creating role")
  class WhenCreatingRole {
    @Test @DisplayName("should succeed with valid name and permissions")
    void shouldSucceedWithValidNameAndPermissions() {
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      assertThat(role.getRoleId()).isNotNull();
      assertThat(role.getRoleName()).isEqualTo(fixtures.adminRoleName());
      assertThat(role.getPermissions()).containsExactly(fixtures.readPermission());
    }

    @Test @DisplayName("should reject creation with empty permissions")
    void shouldRejectCreationWithEmptyPermissions() {
      assertThatThrownBy(() -> Role.createRole(fixtures.adminRoleName(), Set.of()))
          .isInstanceOf(EmptyRoleException.class);
    }

    @Test @DisplayName("should reject creation with null name")
    void shouldRejectCreationWithNullName() {
      assertThatThrownBy(() -> Role.createRole(null, Set.of(fixtures.readPermission())))
          .isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test @DisplayName("should reject creation with null permissions set")
    void shouldRejectCreationWithNullPermissionsSet() {
      assertThatThrownBy(() -> Role.createRole(fixtures.adminRoleName(), null))
          .isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  // ───────────────────── Permission management ────────────────────
  @Nested @DisplayName("When managing permissions")
  class WhenManagingPermissions {
    private Role adminRole;

    @BeforeEach void setUpRole() {
      adminRole = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      adminRole.pullEvents();
    }

    // ───── Single permission ops ─────
    @Nested @DisplayName("Single permission operations")
    class SinglePermissionOperations {
      @Test @DisplayName("should add permission and emit PermissionAdded event")
      void shouldAddPermissionAndEmitEvent() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();
        assertThat(adminRole.pullEvents()).singleElement().isInstanceOf(PermissionAdded.class);
      }

      @Test @DisplayName("should ignore adding duplicate permission")
      void shouldIgnoreAddingDuplicatePermission() {
        adminRole.addPermission(fixtures.readPermission(), NOW);
        assertThat(adminRole.pullEvents()).isEmpty();
      }

      @Test @DisplayName("should remove permission and emit PermissionRemoved event")
      void shouldRemovePermissionAndEmitEvent() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.pullEvents();
        adminRole.removePermission(fixtures.writePermission(), NOW);
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();
        assertThat(adminRole.pullEvents()).singleElement().isInstanceOf(PermissionRemoved.class);
      }

      @Test @DisplayName("should reject removing last permission")
      void shouldRejectRemovingLastPermission() {
        assertThatThrownBy(() -> adminRole.removePermission(fixtures.readPermission(), NOW))
            .isInstanceOf(EmptyRoleException.class);
      }

      @Test @DisplayName("should reject removing non-existent permission")
      void shouldRejectRemovingNonexistentPermission() {
        assertThatThrownBy(() -> adminRole.removePermission(fixtures.deletePermission(), NOW))
            .isInstanceOf(PermissionNotFoundException.class);
      }
    }

    // ───── Bulk permission ops ─────
    @Nested @DisplayName("Bulk permission operations")
    class BulkPermissionOperations {
      @Test @DisplayName("should add multiple permissions and emit events for new ones")
      void shouldAddMultiplePermissionsAndEmitEventsForNewOnes() {
        adminRole.addPermissions(Set.of(fixtures.readPermission(), fixtures.writePermission(), fixtures.deletePermission()), NOW);
        var events = adminRole.pullEvents();
        assertThat(events).hasSize(2).allMatch(PermissionAdded.class::isInstance);
      }

      @Test @DisplayName("should remove multiple permissions and emit corresponding events")
      void shouldRemoveMultiplePermissionsAndEmitEvents() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.addPermission(fixtures.deletePermission(), NOW);
        adminRole.pullEvents();
        adminRole.removePermissions(Set.of(fixtures.writePermission(), fixtures.deletePermission()), NOW);
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();
        assertThat(adminRole.pullEvents()).hasSize(2).allMatch(PermissionRemoved.class::isInstance);
      }

      @Test @DisplayName("should reject removing all permissions")
      void shouldRejectRemovingAllPermissions() {
        adminRole.addPermission(fixtures.writePermission(), NOW);
        adminRole.pullEvents();
        assertThatThrownBy(() -> adminRole.removePermissions(Set.of(fixtures.readPermission(), fixtures.writePermission()), NOW))
            .isInstanceOf(EmptyRoleException.class);
      }

      @Test @DisplayName("should reject removing non-existent permissions in bulk")
      void shouldRejectRemovingNonexistentPermissionsInBulk() {
        assertThatThrownBy(() -> adminRole.removePermissions(Set.of(fixtures.writePermission()), NOW))
            .isInstanceOf(PermissionNotFoundException.class);
      }

      @Test @DisplayName("should reject null set during bulk add/remove")
      void shouldRejectNullSetDuringBulkAddRemove() {
        assertThatThrownBy(() -> adminRole.addPermissions(null, NOW)).isInstanceOf(EntityRequiredFieldException.class);
        assertThatThrownBy(() -> adminRole.removePermissions(null, NOW)).isInstanceOf(EntityRequiredFieldException.class);
      }
    }
  }

  // ───────────────────── Role invariants ───────────────────────
  @Nested @DisplayName("Role invariants")
  class RoleInvariants {
    @Test @DisplayName("should provide unmodifiable permissions collection")
    void shouldProvideUnmodifiablePermissionsCollection() {
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      assertThatThrownBy(() -> role.getPermissions().add(fixtures.writePermission()))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested @DisplayName("Object contract")
    class ObjectContract {
      @Test @DisplayName("should compare roles by roleId only")
      void shouldCompareRolesByRoleIdOnly() {
        var r1 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        var r2 = r1;
        var r3 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        assertThat(r1).isEqualTo(r2).isNotEqualTo(r3);
      }

      @Test @DisplayName("should produce consistent hashCode based on roleId")
      void shouldProduceConsistentHashCodeBasedOnRoleId() {
        var r1 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        var r2 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
        assertThat(r1.hashCode()).isEqualTo(r1.hashCode());
        assertThat(r1.hashCode()).isNotEqualTo(r2.hashCode());
      }

      @Test @DisplayName("should produce descriptive toString representation")
      void shouldProduceDescriptiveToStringRepresentation() {
        var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission(), fixtures.writePermission()));
        assertThat(role.toString())
            .contains("Role")
            .contains(role.getRoleId().toString())
            .contains(fixtures.adminRoleName().toString())
            .contains("permissionsCount=2");
      }
    }
  }
}
