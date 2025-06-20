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
