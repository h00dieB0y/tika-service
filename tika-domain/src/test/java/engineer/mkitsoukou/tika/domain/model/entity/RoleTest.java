package engineer.mkitsoukou.tika.domain.model.entity;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import engineer.mkitsoukou.tika.domain.exception.EmptyRoleException;
import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.exception.PermissionNotFoundException;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;

@DisplayName("Role Entity")
class RoleTest {

  // Test fixtures as records for immutability
  private record RoleFixtures(
      RoleName adminRoleName,
      Permission readPermission,
      Permission writePermission,
      Permission deletePermission,
      Permission updatePermission) {}

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

  @Nested
  @DisplayName("Role Creation")
  class WhenCreatingRole {
    @Test
    @DisplayName("should succeed with valid name and permissions")
    void shouldSucceedWithValidNameAndPermissions() {
      // Act
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));

      // Assert
      assertThat(role.getRoleId()).isNotNull();
      assertThat(role.getRoleName()).isEqualTo(fixtures.adminRoleName());
      assertThat(role.getPermissions())
          .hasSize(1)
          .contains(fixtures.readPermission());
    }

    @Test
    @DisplayName("should reject creation with empty permissions")
    void shouldRejectCreationWithEmptyPermissions() {
      assertThatThrownBy(() ->
          Role.createRole(fixtures.adminRoleName(), Set.of())
      ).isInstanceOf(EmptyRoleException.class);
    }

    @Test
    @DisplayName("should reject creation with null name")
    void shouldRejectCreationWithNullName() {
      assertThatThrownBy(() ->
          Role.createRole(null, Set.of(fixtures.readPermission()))
      ).isInstanceOf(EntityRequiredFieldException.class);
    }

    @Test
    @DisplayName("should reject creation with null permissions set")
    void shouldRejectCreationWithNullPermissionsSet() {
      assertThatThrownBy(() ->
          Role.createRole(fixtures.adminRoleName(), null)
      ).isInstanceOf(EntityRequiredFieldException.class);
    }
  }

  @Nested
  @DisplayName("Permission Management")
  class WhenManagingPermissions {
    private Role adminRole;

    @BeforeEach
    void prepareRoleWithBasicPermission() {
      adminRole = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      adminRole.pullEvents(); // Clear creation events
    }

    @Nested
    @DisplayName("Single permission operations")
    class SinglePermissionOperations {
      @Test
      @DisplayName("should add permission and emit PermissionAdded event")
      void shouldAddPermissionAndEmitEvent() {
        // Act
        adminRole.addPermission(fixtures.writePermission());

        // Assert permission added
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();

        // Assert event
        var events = adminRole.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(PermissionAdded.class)
            .extracting(PermissionAdded.class::cast)
            .returns(adminRole.getRoleId(), PermissionAdded::getRoleId)
            .returns(fixtures.writePermission(), PermissionAdded::getPermission);
      }

      @Test
      @DisplayName("should ignore adding duplicate permission")
      void shouldIgnoreAddingDuplicatePermission() {
        // Act
        adminRole.addPermission(fixtures.readPermission());

        // Assert
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue();
        assertThat(adminRole.pullEvents()).isEmpty();
      }

      @Test
      @DisplayName("should remove permission and emit PermissionRemoved event")
      void shouldRemovePermissionAndEmitEvent() {
        // Arrange
        adminRole.addPermission(fixtures.writePermission());
        adminRole.pullEvents(); // Clear add event

        // Act
        adminRole.removePermission(fixtures.writePermission());

        // Assert permission removed
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();

        // Assert event
        var events = adminRole.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(PermissionRemoved.class)
            .extracting(PermissionRemoved.class::cast)
            .returns(adminRole.getRoleId(), PermissionRemoved::getRoleId)
            .returns(fixtures.writePermission(), PermissionRemoved::getPermission);
      }

      @Test
      @DisplayName("should reject removing last permission")
      void shouldRejectRemovingLastPermission() {
        assertThatThrownBy(() ->
            adminRole.removePermission(fixtures.readPermission())
        ).isInstanceOf(EmptyRoleException.class);
      }

      @Test
      @DisplayName("should reject removing non-existent permission")
      void shouldRejectRemovingNonexistentPermission() {
        var nonExistentPermission = new Permission("non.existent");

        assertThatThrownBy(() ->
            adminRole.removePermission(nonExistentPermission)
        ).isInstanceOf(PermissionNotFoundException.class);
      }

      @Test
      @DisplayName("should reject null permission during addition")
      void shouldRejectNullPermissionDuringAddition() {
        assertThatThrownBy(() ->
            adminRole.addPermission(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should reject null permission during removal")
      void shouldRejectNullPermissionDuringRemoval() {
        assertThatThrownBy(() ->
            adminRole.removePermission(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should reject null permission during permission check")
      void shouldRejectNullPermissionDuringPermissionCheck() {
        assertThatThrownBy(() ->
            adminRole.hasPermission(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }
    }

    @Nested
    @DisplayName("Bulk permission operations")
    class BulkPermissionOperations {
      @Test
      @DisplayName("should add multiple permissions and emit events")
      void shouldAddMultiplePermissionsAndEmitEvents() {
        // Arrange
        var permissionsToAdd = Set.of(fixtures.writePermission(), fixtures.deletePermission());

        // Act
        adminRole.addPermissions(permissionsToAdd);

        // Assert permissions added
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();
        assertThat(adminRole.hasPermission(fixtures.deletePermission())).isTrue();

        // Assert events
        var events = adminRole.pullEvents();
        assertThat(events)
            .hasSize(2)
            .allMatch(PermissionAdded.class::isInstance);

        // Verify each permission has an event
        assertThat(events)
            .extracting(event -> ((PermissionAdded)event).getPermission())
            .containsExactlyInAnyOrder(fixtures.writePermission(), fixtures.deletePermission());
      }

      @Test
      @DisplayName("should only emit events for new permissions when adding mix of new and existing")
      void shouldOnlyEmitEventsForNewPermissionsWhenAddingMixOfNewAndExisting() {
        // Arrange
        var permissionsToAdd = Set.of(fixtures.readPermission(), fixtures.writePermission());

        // Act
        adminRole.addPermissions(permissionsToAdd);

        // Assert all permissions exist
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue();
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();

        // Assert only one event for the new permission
        var events = adminRole.pullEvents();
        assertThat(events).hasSize(1);

        assertThat(events.getFirst())
            .isInstanceOf(PermissionAdded.class)
            .extracting(PermissionAdded.class::cast)
            .returns(fixtures.writePermission(), PermissionAdded::getPermission);
      }

      @Test
      @DisplayName("should reject null permissions set during bulk addition")
      void shouldRejectNullPermissionsSetDuringBulkAddition() {
        assertThatThrownBy(() ->
            adminRole.addPermissions(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }

      @Test
      @DisplayName("should remove multiple permissions and emit events")
      void shouldRemoveMultiplePermissionsAndEmitEvents() {
        // Arrange - add additional permissions
        adminRole.addPermission(fixtures.writePermission());
        adminRole.addPermission(fixtures.deletePermission());
        adminRole.pullEvents(); // Clear add events

        // Act
        adminRole.removePermissions(Set.of(fixtures.writePermission(), fixtures.deletePermission()));

        // Assert permissions removed
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isFalse();
        assertThat(adminRole.hasPermission(fixtures.deletePermission())).isFalse();
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue(); // Original permission remains

        // Assert events
        var events = adminRole.pullEvents();
        assertThat(events)
            .hasSize(2)
            .allMatch(PermissionRemoved.class::isInstance);

        // Verify each permission has a removal event
        assertThat(events)
            .extracting(event -> ((PermissionRemoved)event).getPermission())
            .containsExactlyInAnyOrder(fixtures.writePermission(), fixtures.deletePermission());
      }

      @Test
      @DisplayName("should reject removing all permissions")
      void shouldRejectRemovingAllPermissions() {
        // Arrange - add write permission
        adminRole.addPermission(fixtures.writePermission());
        adminRole.pullEvents(); // Clear add event

        // Act & Assert
        assertThatThrownBy(() ->
            adminRole.removePermissions(Set.of(fixtures.readPermission(), fixtures.writePermission()))
        ).isInstanceOf(EmptyRoleException.class);

        // Verify no permissions were removed
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue();
        assertThat(adminRole.hasPermission(fixtures.writePermission())).isTrue();
      }

      @Test
      @DisplayName("should reject removing non-existent permissions in bulk")
      void shouldRejectRemovingNonexistentPermissionsInBulk() {
        // Act & Assert
        assertThatThrownBy(() ->
            adminRole.removePermissions(Set.of(fixtures.writePermission(), fixtures.deletePermission()))
        ).isInstanceOf(PermissionNotFoundException.class);

        // Verify the role remains unchanged
        assertThat(adminRole.getPermissions()).hasSize(1);
        assertThat(adminRole.hasPermission(fixtures.readPermission())).isTrue();
      }

      @Test
      @DisplayName("should reject null permissions set during bulk removal")
      void shouldRejectNullPermissionsSetDuringBulkRemoval() {
        assertThatThrownBy(() ->
            adminRole.removePermissions(null)
        ).isInstanceOf(EntityRequiredFieldException.class);
      }
    }
  }

  @Nested
  @DisplayName("Role Invariants")
  class RoleInvariants {
    @Test
    @DisplayName("should provide unmodifiable permissions collection")
    void shouldProvideUnmodifiablePermissionsCollection() {
      // Arrange
      var role = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));

      // Act
      var permissions = role.getPermissions();

      // Assert
      assertThatThrownBy(() ->
          permissions.add(fixtures.writePermission())
      ).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should compare roles based only on roleId")
    void shouldCompareRolesBasedOnlyOnRoleId() {
      // Arrange
      var role1 = Role.createRole(fixtures.adminRoleName(), Set.of(fixtures.readPermission()));
      var role2 = Role.createRole(fixtures.adminRoleName(),
          Set.of(fixtures.readPermission(), fixtures.writePermission()));
      var role3 = Role.createRole(new RoleName("DIFFERENT_ROLE"), Set.of(fixtures.readPermission()));

      // Assert different instances are not equal
      assertThat(role1)
          .isNotNull()
          .isNotEqualTo(role2)
          .isNotEqualTo(role3)
          .isNotEqualTo(new Object());

    }
  }
}
