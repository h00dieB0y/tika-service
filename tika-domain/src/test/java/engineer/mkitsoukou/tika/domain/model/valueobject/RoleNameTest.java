package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("RoleName value object")
class RoleNameTest {

  @Nested
  @DisplayName("Null value handling")
  class NullValueHandling {
    @Test
    @DisplayName("Given null role name when creating RoleName then throws InvalidRoleNameException")
    void givenNullRoleName_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      assertThatThrownBy(() -> new RoleName(null))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("Role name must not be null or blank");
    }

    @Test
    @DisplayName("Given null role name when using factory method then throws InvalidRoleNameException")
    void givenNullRoleName_whenUsingFactoryMethod_thenThrowsInvalidRoleNameException() {
      String nullRoleName = null;
      assertThatThrownBy(() -> RoleName.of(nullRoleName))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("Role name must not be null or blank");
    }
  }

  @Nested
  @DisplayName("Valid instance creation")
  class ValidInstanceCreation {
    @Test
    @DisplayName("Given valid role name when creating RoleName then creates successfully")
    void givenValidRoleName_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given
      var validRoleName = "ADMIN";

      // When
      var roleName = new RoleName(validRoleName);

      // Then
      assertThat(roleName.name()).isEqualTo(validRoleName);
    }

    @Test
    @DisplayName("Given valid role name when using factory method then creates successfully")
    void givenValidRoleName_whenUsingFactoryMethod_thenCreatesSuccessfully() {
      // Given
      var validRoleName = "ADMIN";

      // When
      var roleName = RoleName.of(validRoleName);

      // Then
      assertThat(roleName.name()).isEqualTo(validRoleName);
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "ADMIN",
      "USER",
      "ROLE_ADMIN",
      "ROLE_USER",
      "SUPER_ADMIN",
      "GUEST_USER",
      "SYSTEM_ADMIN",
      "APP_MANAGER",
      "READ_ONLY_USER",
      "A",
      "ROLE_A",
      "Z9",
      "ADMIN123"
    })
    @DisplayName("Given various valid role names when creating RoleName then creates successfully")
    void givenVariousValidRoleNames_whenCreatingRoleName_thenCreatesSuccessfully(String validRoleName) {
      assertDoesNotThrow(() -> RoleName.of(validRoleName));
    }
  }

  @Nested
  @DisplayName("Multiple instance behavior")
  class MultipleInstanceBehavior {
    @Test
    @DisplayName("Given different role names when comparing then they are not equal")
    void givenDifferentRoleNames_whenComparing_thenTheyAreNotEqual() {
      // Given
      var firstRoleName = RoleName.of("ADMIN");
      var secondRoleName = RoleName.of("USER");

      // Then
      assertThat(firstRoleName).isNotEqualTo(secondRoleName);
    }
  }

  @Nested
  @DisplayName("Boundary behaviors")
  class BoundaryBehavior {
    @Test
    @DisplayName("Given role name with maximum length when creating RoleName then creates successfully")
    void givenRoleNameWithMaximumLength_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given - 100 characters is the maximum
      var maxLengthRoleName = "ROLE_" + "A".repeat(95);

      // Then
      assertDoesNotThrow(() -> RoleName.of(maxLengthRoleName));
      assertThat(maxLengthRoleName)
        .hasSize(100)
        .matches("ROLE_[A-Z0-9_]{0,95}");
    }

    @Test
    @DisplayName("Given role name above maximum length when creating RoleName then throws InvalidRoleNameException")
    void givenRoleNameAboveMaximumLength_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      // Given - 101 characters is above maximum
      var tooLongRoleName = "ROLE_" + "A".repeat(96);

      // Then
      assertThatThrownBy(() -> RoleName.of(tooLongRoleName))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("Role name must not exceed 100 characters");

      assertThat(tooLongRoleName)
        .hasSizeGreaterThan(100)
        .doesNotMatch("ROLE_[A-Z0-9_]{0,95}");
    }

    @Test
    @DisplayName("Given role name with maximum segments when creating RoleName then creates successfully")
    void givenRoleNameWithMaximumSegments_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given - max 3 segments (base + 2 additional)
      var maxSegmentsRoleName = "ROLE_ADMIN_USER_MANAGER";

      // Then
      assertDoesNotThrow(() -> RoleName.of(maxSegmentsRoleName));
    }

    @Test
    @DisplayName("Given role name with too many segments when creating RoleName then throws InvalidRoleNameException")
    void givenRoleNameWithTooManySegments_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      // Given - 4 segments is too many
      var tooManySegmentsRoleName = "ROLE_ADMIN_USER_MANAGER_EXTRA";

      // Then
      assertThatThrownBy(() -> RoleName.of(tooManySegmentsRoleName))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("must be a valid role name format");
    }

    @Test
    @DisplayName("Given role name with maximum segment length when creating RoleName then creates successfully")
    void givenRoleNameWithMaximumSegmentLength_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given - segment max length is 30
      var segmentMaxLength = "A".repeat(30);
      var roleNameWithMaxSegmentLength = "ROLE_" + segmentMaxLength + "_" + segmentMaxLength;

      // Then
      assertDoesNotThrow(() -> RoleName.of(roleNameWithMaxSegmentLength));
    }
  }

  @Nested
  @DisplayName("Object contract compliance")
  class ObjectContractBehavior {
    @Test
    @DisplayName("Given same role name when comparing RoleNames then they are equal")
    void givenSameRoleName_whenComparingRoleNames_thenTheyAreEqual() {
      // Given
      var roleNameValue = "ADMIN";
      var firstRoleName = RoleName.of(roleNameValue);
      var secondRoleName = RoleName.of(roleNameValue);

      // Then
      assertThat(firstRoleName).isEqualTo(secondRoleName)
        .hasSameHashCodeAs(roleNameValue);
    }

    @Test
    @DisplayName("Given RoleName when calling toString then returns role name string")
    void givenRoleName_whenCallingToString_thenReturnsRoleNameString() {
      // Given
      var roleNameValue = "ADMIN";
      var roleName = RoleName.of(roleNameValue);

      // When
      var stringRepresentation = roleName.toString();

      // Then
      assertThat(stringRepresentation).isEqualTo(roleNameValue);
    }
  }

  @Nested
  @DisplayName("Exception handling")
  class InvalidInputHandling {
    @Test
    @DisplayName("Given empty string when creating RoleName then throws InvalidRoleNameException")
    void givenEmptyString_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      assertThatThrownBy(() -> RoleName.of(""))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("Role name must not be null or blank");
    }

    @Test
    @DisplayName("Given blank string when creating RoleName then throws InvalidRoleNameException")
    void givenBlankString_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      assertThatThrownBy(() -> RoleName.of("  "))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("Role name must not be null or blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "admin",                      // Lowercase not allowed
      "Admin",                      // Mixed case not allowed
      "role_ADMIN",                 // Lowercase in prefix
      "ROLE-ADMIN",                 // Hyphen not allowed
      "123ADMIN",                   // Starting with number
      "_ADMIN",                     // Starting with underscore
      "ADMIN_",                     // Ending with underscore
      "ADMIN__USER",                // Double underscore
      "ROLE_admin",                 // Lowercase after prefix
      "R0LE_ADMIN",                 // Number in ROLE prefix
      "A1_USER",                    // Digit before underscore
      "ROLE_ADMIN_USER_MANAGER_X",  // Too many segments (4)
    })
    @DisplayName("Given invalid role name format when creating RoleName then throws InvalidRoleNameException")
    void givenInvalidRoleNameFormat_whenCreatingRoleName_thenThrowsInvalidRoleNameException(String invalidRoleName) {
      assertThatThrownBy(() -> RoleName.of(invalidRoleName))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("must be a valid role name format");
    }

    @Test
    @DisplayName("Given role name with digit before underscore when creating RoleName then throws InvalidRoleNameException")
    void givenRoleNameWithDigitBeforeUnderscore_whenCreatingRoleName_thenThrowsInvalidRoleNameException() {
      // Given
      var roleNameWithDigitBeforeUnderscore = "ADMIN1_USER";

      // Then
      assertThatThrownBy(() -> RoleName.of(roleNameWithDigitBeforeUnderscore))
        .isInstanceOf(InvalidRoleNameException.class)
        .hasMessageContaining("must be a valid role name format");
    }
  }

  @Nested
  @DisplayName("Common use cases")
  class CommonUseCases {
    @Test
    @DisplayName("Given role name with ROLE_ prefix when creating RoleName then creates successfully")
    void givenRoleNameWithRolePrefix_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given
      var roleNameWithPrefix = "ROLE_ADMIN";

      // When
      var roleName = RoleName.of(roleNameWithPrefix);

      // Then
      assertThat(roleName.name()).isEqualTo(roleNameWithPrefix);
    }

    @Test
    @DisplayName("Given role name with numbers when creating RoleName then creates successfully")
    void givenRoleNameWithNumbers_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given
      var roleNameWithNumbers = "ADMIN123";

      // When
      var roleName = RoleName.of(roleNameWithNumbers);

      // Then
      assertThat(roleName.name()).isEqualTo(roleNameWithNumbers);
    }

    @Test
    @DisplayName("Given role name with multiple segments when creating RoleName then creates successfully")
    void givenRoleNameWithMultipleSegments_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given
      var roleNameWithSegments = "SYSTEM_ADMIN_LEVEL1";

      // When
      var roleName = RoleName.of(roleNameWithSegments);

      // Then
      assertThat(roleName.name()).isEqualTo(roleNameWithSegments);
    }

    @Test
    @DisplayName("Given role name with digits after underscore when creating RoleName then creates successfully")
    void givenRoleNameWithDigitsAfterUnderscore_whenCreatingRoleName_thenCreatesSuccessfully() {
      // Given
      var roleNameWithDigitsAfterUnderscore = "ADMIN_123USER";

      // When
      var roleName = RoleName.of(roleNameWithDigitsAfterUnderscore);

      // Then
      assertThat(roleName.name()).isEqualTo(roleNameWithDigitsAfterUnderscore);
    }
  }
}

