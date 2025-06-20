package engineer.mkitsoukou.tika.domain.model.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import engineer.mkitsoukou.tika.domain.exception.EntityRequiredFieldException;
import engineer.mkitsoukou.tika.domain.model.event.PermissionAdded;
import engineer.mkitsoukou.tika.domain.model.event.PermissionRemoved;
import engineer.mkitsoukou.tika.domain.model.valueobject.Permission;
import engineer.mkitsoukou.tika.domain.model.valueobject.RoleName;

class RoleTest {

  private RoleName roleName;
  private Permission readPermission;
  private Permission writePermission;

  @BeforeEach
  void setUp() {
    roleName = new RoleName("ADMIN_ROLE");
    readPermission = new Permission("resource.read");
    writePermission = new Permission("resource.write");
  }

  @Nested
  class CreationTests {
    @Test
    void roleCreationWithValidDataSucceeds() {
      var role = Role.of(roleName, Set.of(readPermission));

      assertNotNull(role.getRoleId());
      assertEquals(roleName, role.getRoleName());
      assertTrue(role.getPermissions().contains(readPermission));
      assertEquals(1, role.getPermissions().size());
    }

    @Test
    void roleCreationWithNoPermissionsSucceeds() {
      var role = Role.of(roleName, Set.of());

      assertNotNull(role.getRoleId());
      assertEquals(roleName, role.getRoleName());
      assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void roleCreationWithNullNameThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> Role.of(null, Set.of())
      );
    }

    @Test
    void roleCreationWithNullPermissionsSetThrows() {
      assertThrows(
        EntityRequiredFieldException.class,
        () -> Role.of(roleName, null)
      );
    }
  }

  @Nested
  class PermissionManagementTests {
    private Role role;

    @BeforeEach
    void createRole() {
      role = Role.of(roleName, Set.of());
      role.pullEvents(); // Clear creation events
    }

    @Test
    void addingPermissionEmitsEvent() {
      role.addPermission(readPermission);

      assertTrue(role.hasPermission(readPermission));

      var events = role.pullEvents();
      assertEquals(1, events.size());

      var event = assertInstanceOf(PermissionAdded.class, events.getFirst());
      assertEquals(role.getRoleId(), event.getRoleId());
      assertEquals(readPermission, event.getPermission());
    }

    @Test
    void addingExistingPermissionDoesNothing() {
      role.addPermission(readPermission);
      role.pullEvents(); // Clear first event

      role.addPermission(readPermission);

      assertTrue(role.hasPermission(readPermission));
      assertTrue(role.pullEvents().isEmpty()); // No new events
    }

    @Test
    void removingPermissionEmitsEvent() {
      role.addPermission(readPermission);
      role.pullEvents(); // Clear add event

      role.removePermission(readPermission);

      assertFalse(role.hasPermission(readPermission));

      var events = role.pullEvents();
      assertEquals(1, events.size());

      var event = assertInstanceOf(PermissionRemoved.class, events.getFirst());
      assertEquals(role.getRoleId(), event.getRoleId());
      assertEquals(readPermission, event.getPermission());
    }

    @Test
    void removingNonExistentPermissionDoesNothing() {
      role.removePermission(readPermission);

      assertFalse(role.hasPermission(readPermission));
      assertTrue(role.pullEvents().isEmpty()); // No events
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
  }

  @Test
  void permissionsCollectionIsUnmodifiable() {
    var role = Role.of(roleName, Set.of(readPermission));

    var permissions = role.getPermissions();

    assertThrows(
      UnsupportedOperationException.class,
      () -> permissions.add(writePermission)
    );
  }

  @Test
  void equalsOnlyChecksRoleId() {
    var role1 = Role.of(roleName, Set.of(readPermission));
    var role2 = Role.of(roleName, Set.of(readPermission, writePermission));
    var role3 = Role.of(new RoleName("DIFFERENT_ROLE"), Set.of());

    // Different instances with same data should not be equal
    assertNotEquals(role1, role2);
    assertNotEquals(role1, role3);

    // A role should equal itself
    assertEquals(role1, role1);

    // A role should not equal null or other types
    assertNotEquals(role1, null);
    assertNotEquals(role1, "not a role");
  }
}
