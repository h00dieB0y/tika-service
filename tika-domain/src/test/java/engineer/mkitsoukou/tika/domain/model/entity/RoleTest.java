package engineer.mkitsoukou.tika.domain.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import engineer.mkitsoukou.tika.domain.exception.EmptyRoleException;
import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.exception.PermissionNotFoundException;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;

class RoleTest {

  private RoleName roleName;
  private Permission readPermission;
  private Permission writePermission;
  private Permission deletePermission;
  private Permission updatePermission;

  @BeforeEach
  void setUp() {
    roleName = new RoleName("ADMIN_ROLE");
    readPermission = new Permission("resource.read");
    writePermission = new Permission("resource.write");
    deletePermission = new Permission("resource.delete");
    updatePermission = new Permission("resource.update");
  }

  @Nested
  class CreationTests {
    @Test
    void roleCreationWithValidDataSucceeds() {
      var role = Role.createRole(roleName, Set.of(readPermission));

      assertNotNull(role.getRoleId());
      assertEquals(roleName, role.getRoleName());
      assertTrue(role.getPermissions().contains(readPermission));
      assertEquals(1, role.getPermissions().size());
    }

    @Test
    void roleCreationWithNoPermissionsThrows() {
      assertThrows(
        EmptyRoleException.class,
        () -> Role.createRole(roleName, Set.of())
      );
    }

    @Test
    void roleCreationWithNullNameThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> Role.createRole(null, Set.of(readPermission))
      );
    }

    @Test
    void roleCreationWithNullPermissionsSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> Role.createRole(roleName, null)
      );
    }
  }

  @Nested
  class PermissionManagementTests {
    private Role role;

    @BeforeEach
    void createRole() {
      role = Role.createRole(roleName, Set.of(readPermission));
      role.pullEvents(); // Clear creation events
    }

    @Test
    void addingPermissionEmitsEvent() {
      role.addPermission(writePermission);

      assertTrue(role.hasPermission(writePermission));

      var events = role.pullEvents();
      assertEquals(1, events.size());

      var event = assertInstanceOf(PermissionAdded.class, events.getFirst());
      assertEquals(role.getRoleId(), event.getRoleId());
      assertEquals(writePermission, event.getPermission());
    }

    @Test
    void addingExistingPermissionDoesNothing() {
      role.addPermission(readPermission);

      assertTrue(role.hasPermission(readPermission));
      assertTrue(role.pullEvents().isEmpty()); // No new events
    }

    @Test
    void removingPermissionEmitsEvent() {
      role.addPermission(writePermission);
      role.pullEvents(); // Clear add event

      role.removePermission(writePermission);

      assertFalse(role.hasPermission(writePermission));

      var events = role.pullEvents();
      assertEquals(1, events.size());

      var event = assertInstanceOf(PermissionRemoved.class, events.getFirst());
      assertEquals(role.getRoleId(), event.getRoleId());
      assertEquals(writePermission, event.getPermission());
    }

    @Test
    void removingLastPermissionThrows() {
      assertThrows(
        EmptyRoleException.class,
        () -> role.removePermission(readPermission)
      );
    }

    @Test
    void removingNonExistentPermissionThrows() {
      Permission nonExistentPermission = new Permission("non.existent");

      assertThrows(
        PermissionNotFoundException.class,
        () -> role.removePermission(nonExistentPermission)
      );
    }

    @Test
    void addingNullPermissionThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> role.addPermission(null)
      );
    }

    @Test
    void removingNullPermissionThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> role.removePermission(null)
      );
    }

    @Test
    void checkingNullPermissionThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> role.hasPermission(null)
      );
    }

    @Test
    void addingMultiplePermissionsEmitsEvents() {
      Set<Permission> permissionsToAdd = Set.of(writePermission, deletePermission);

      role.addPermissions(permissionsToAdd);

      assertTrue(role.hasPermission(writePermission));
      assertTrue(role.hasPermission(deletePermission));

      var events = role.pullEvents();
      assertEquals(2, events.size());

      // Check that we have PermissionAdded events for both permissions
      boolean foundWriteEvent = false;
      boolean foundDeleteEvent = false;

      for (var event : events) {
        var permissionEvent = assertInstanceOf(PermissionAdded.class, event);
        assertEquals(role.getRoleId(), permissionEvent.getRoleId());

        if (permissionEvent.getPermission().equals(writePermission)) {
          foundWriteEvent = true;
        } else if (permissionEvent.getPermission().equals(deletePermission)) {
          foundDeleteEvent = true;
        }
      }

      assertTrue(foundWriteEvent, "Should have an event for adding write permission");
      assertTrue(foundDeleteEvent, "Should have an event for adding delete permission");
    }

    @Test
    void addingMultiplePermissionsIncludingExistingOnesEmitsEventsOnlyForNew() {
      Set<Permission> permissionsToAdd = Set.of(readPermission, writePermission);

      role.addPermissions(permissionsToAdd);

      assertTrue(role.hasPermission(readPermission));
      assertTrue(role.hasPermission(writePermission));

      var events = role.pullEvents();
      assertEquals(1, events.size());

      var event = assertInstanceOf(PermissionAdded.class, events.getFirst());
      assertEquals(role.getRoleId(), event.getRoleId());
      assertEquals(writePermission, event.getPermission());
    }

    @Test
    void addingNullPermissionsSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> role.addPermissions(null)
      );
    }

    @Test
    void removingMultiplePermissionsEmitsEvents() {
      // Setup role with multiple permissions
      role.addPermission(writePermission);
      role.addPermission(deletePermission);
      role.pullEvents(); // Clear add events

      Set<Permission> permissionsToRemove = Set.of(writePermission, deletePermission);

      role.removePermissions(permissionsToRemove);

      assertFalse(role.hasPermission(writePermission));
      assertFalse(role.hasPermission(deletePermission));
      assertTrue(role.hasPermission(readPermission)); // Original permission remains

      var events = role.pullEvents();
      assertEquals(2, events.size());

      // Check that we have PermissionRemoved events for both permissions
      boolean foundWriteEvent = false;
      boolean foundDeleteEvent = false;

      for (var event : events) {
        var permissionEvent = assertInstanceOf(PermissionRemoved.class, event);
        assertEquals(role.getRoleId(), permissionEvent.getRoleId());

        if (permissionEvent.getPermission().equals(writePermission)) {
          foundWriteEvent = true;
        } else if (permissionEvent.getPermission().equals(deletePermission)) {
          foundDeleteEvent = true;
        }
      }

      assertTrue(foundWriteEvent, "Should have an event for removing write permission");
      assertTrue(foundDeleteEvent, "Should have an event for removing delete permission");
    }

    @Test
    void removingAllPermissionsThrows() {
      // Setup role with multiple permissions
      role.addPermission(writePermission);
      role.pullEvents(); // Clear add events

      Set<Permission> allPermissions = Set.of(readPermission, writePermission);

      assertThrows(
        EmptyRoleException.class,
        () -> role.removePermissions(allPermissions)
      );

      // Verify no permissions were removed
      assertTrue(role.hasPermission(readPermission));
      assertTrue(role.hasPermission(writePermission));
    }

    @Test
    void removingNonExistentPermissionsThrows() {
      Set<Permission> permissionsToRemove = Set.of(writePermission, deletePermission);

      assertThrows(
        PermissionNotFoundException.class,
        () -> role.removePermissions(permissionsToRemove)
      );

      // Verify the role remains unchanged
      assertTrue(role.hasPermission(readPermission));
      assertEquals(1, role.getPermissions().size());
    }

    @Test
    void removingNullPermissionsSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> role.removePermissions(null)
      );
    }
  }

  @Test
  void permissionsCollectionIsUnmodifiable() {
    var role = Role.createRole(roleName, Set.of(readPermission));

    var permissions = role.getPermissions();

    assertThrows(
      UnsupportedOperationException.class,
      () -> permissions.add(writePermission)
    );
  }

  @Test
  void equalsOnlyChecksRoleId() {
    var role1 = Role.createRole(roleName, Set.of(readPermission));
    var role2 = Role.createRole(roleName, Set.of(readPermission, writePermission));
    var role3 = Role.createRole(new RoleName("DIFFERENT_ROLE"), Set.of(readPermission));

    // Different instances with same data should not be equal
    assertNotEquals(role1, role2);
    assertNotEquals(role1, role3);

    // A role should equal itself
    assertEquals(role1, role1);

    // A role should not equal null or other types
    assertNotEquals(null, role1);
    assertNotEquals("not a role", role1);
  }
}
