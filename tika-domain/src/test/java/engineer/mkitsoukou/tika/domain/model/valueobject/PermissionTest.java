package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPermissionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Permission value object")
class PermissionTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null permission when creating Permission then throws InvalidPermissionException")
        void givenNullPermission_whenCreatingPermission_thenThrowsInvalidPermissionException() {
            assertThatThrownBy(() -> new Permission(null))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("Permission must not be null or blank");
        }

        @Test
        @DisplayName("Given null permission when using factory method then throws InvalidPermissionException")
        void givenNullPermission_whenUsingFactoryMethod_thenThrowsInvalidPermissionException() {
            String nullPermission = null;
            assertThatThrownBy(() -> Permission.of(nullPermission))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("Permission must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidInstanceCreation {
        @Test
        @DisplayName("Given valid permission when creating Permission then creates successfully")
        void givenValidPermission_whenCreatingPermission_thenCreatesSuccessfully() {
            // Given
            var validPermission = "user.read";

            // When
            var permission = new Permission(validPermission);

            // Then
            assertThat(permission.value()).isEqualTo(validPermission);
        }

        @Test
        @DisplayName("Given valid permission when using factory method then creates successfully")
        void givenValidPermission_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validPermission = "user.read";

            // When
            var permission = Permission.of(validPermission);

            // Then
            assertThat(permission.value()).isEqualTo(validPermission);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user.read",
            "role.create",
            "organization.update",
            "document.delete",
            "system.admin.access",
            "api.resource.read",
            "report.financial.generate",
            "user-management.user.create",
            "auth.token.validate"
        })
        @DisplayName("Given various valid permissions when creating Permission then creates successfully")
        void givenVariousValidPermissions_whenCreatingPermission_thenCreatesSuccessfully(String validPermission) {
            assertDoesNotThrow(() -> Permission.of(validPermission));
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given different permissions when comparing then they are not equal")
        void givenDifferentPermissions_whenComparing_thenTheyAreNotEqual() {
            // Given
            var firstPermission = Permission.of("user.read");
            var secondPermission = Permission.of("user.write");

            // Then
            assertThat(firstPermission).isNotEqualTo(secondPermission);
        }
    }

    @Nested
    @DisplayName("Boundary behaviors")
    class BoundaryBehavior {
        @Test
        @DisplayName("Given permission with maximum length when creating Permission then creates successfully")
        void givenPermissionWithMaximumLength_whenCreatingPermission_thenCreatesSuccessfully() {
            // Given - 100 characters is the maximum
            var segment1 = "abcdefghij";
            var segment2 = "klmnopqrst";
            var segment3 = "uvwxyzabcd";
            var segment4 = "efghijklmn";
            var segment5 = "opqrstuvwx";
            var segment6 = "yzabcdefgh";
            var segment7 = "ijklmnopqr";
            var segment8 = "stuvwxyzab";
            var segment9 = "cdefghijkl";
            var segment10 = "m";

            var maxLengthPermission = segment1 + "." + segment2 + "." + segment3 + "." +
                                      segment4 + "." + segment5 + "." + segment6 + "." +
                                      segment7 + "." + segment8 + "." + segment9 + "." + segment10;

            // Then
            assertDoesNotThrow(() -> Permission.of(maxLengthPermission));
            assertThat(maxLengthPermission).hasSize(100);
        }

        @Test
        @DisplayName("Given permission above maximum length when creating Permission then throws InvalidPermissionException")
        void givenPermissionAboveMaximumLength_whenCreatingPermission_thenThrowsInvalidPermissionException() {
            // Given - 101 characters is above maximum
            var segment1 = "abcdefghij";
            var segment2 = "klmnopqrst";
            var segment3 = "uvwxyzabcd";
            var segment4 = "efghijklmn";
            var segment5 = "opqrstuvwx";
            var segment6 = "yzabcdefgh";
            var segment7 = "ijklmnopqr";
            var segment8 = "stuvwxyzab";
            var segment9 = "cdefghijkl";
            var segment10 = "mn";

            var tooLongPermission = segment1 + "." + segment2 + "." + segment3 + "." +
                                   segment4 + "." + segment5 + "." + segment6 + "." +
                                   segment7 + "." + segment8 + "." + segment9 + "." + segment10;

            // Then
            assertThatThrownBy(() -> Permission.of(tooLongPermission))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("Permission must not exceed 100 characters");

            assertThat(tooLongPermission).hasSize(101);
        }
    }

    @Nested
    @DisplayName("Object contract compliance")
    class ObjectContractBehavior {
        @Test
        @DisplayName("Given same permission when comparing Permissions then they are equal")
        void givenSamePermission_whenComparingPermissions_thenTheyAreEqual() {
            // Given
            var permissionValue = "user.read";
            var firstPermission = Permission.of(permissionValue);
            var secondPermission = Permission.of(permissionValue);

            // Then
            assertThat(firstPermission).isEqualTo(secondPermission)
                .hasSameHashCodeAs(permissionValue);
        }

        @Test
        @DisplayName("Given Permission when calling toString then returns permission string")
        void givenPermission_whenCallingToString_thenReturnsPermissionString() {
            // Given
            var permissionValue = "user.read";
            var permission = Permission.of(permissionValue);

            // When
            var stringRepresentation = permission.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo(permissionValue);
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @Test
        @DisplayName("Given empty string when creating Permission then throws InvalidPermissionException")
        void givenEmptyString_whenCreatingPermission_thenThrowsInvalidPermissionException() {
            assertThatThrownBy(() -> Permission.of(""))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("Permission must not be null or blank");
        }

        @Test
        @DisplayName("Given blank string when creating Permission then throws InvalidPermissionException")
        void givenBlankString_whenCreatingPermission_thenThrowsInvalidPermissionException() {
            assertThatThrownBy(() -> Permission.of("  "))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("Permission must not be null or blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user",                    // Missing dot separator
            "User.read",               // Uppercase first letter
            "user.Read",               // Uppercase after dot
            "1user.read",              // Starts with a number
            "user.read.",              // Ends with a dot
            ".user.read",              // Starts with a dot
            "user..read",              // Double dot
            "user.read#write",         // Invalid character
            "user._read",              // Underscore not allowed
            "very-long-permission$"    // Invalid character
        })
        @DisplayName("Given invalid permission format when creating Permission then throws InvalidPermissionException")
        void givenInvalidPermissionFormat_whenCreatingPermission_thenThrowsInvalidPermissionException(String invalidPermission) {
            assertThatThrownBy(() -> Permission.of(invalidPermission))
                .isInstanceOf(InvalidPermissionException.class)
                .hasMessageContaining("must be a valid permission format");
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given permission with multiple segments when creating Permission then creates successfully")
        void givenPermissionWithMultipleSegments_whenCreatingPermission_thenCreatesSuccessfully() {
            // Given
            var complexPermission = "system.user.profile.read";

            // When
            var permission = Permission.of(complexPermission);

            // Then
            assertThat(permission.value()).isEqualTo(complexPermission);
        }
    }
}
