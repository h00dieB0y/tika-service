package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidRoleIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RoleId value object")
class RoleIdTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null UUID when creating RoleId then throws NullPointerException")
        void givenNullUuid_whenCreatingRoleId_thenThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () -> new RoleId(null));
        }

        @Test
        @DisplayName("Given null UUID when using factory method then throws NullPointerException")
        void givenNullUuid_whenUsingFactoryMethod_thenThrowsNullPointerException() {
            UUID nullUuid = null;
            assertThrows(NullPointerException.class, () -> RoleId.of(nullUuid));
        }

        @Test
        @DisplayName("Given null string when using factory method then throws NullPointerException")
        void givenNullString_whenUsingFactoryMethod_thenThrowsNullPointerException() {
            String nullString = null;
            assertThrows(NullPointerException.class, () -> RoleId.of(nullString));
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidInstanceCreation {
        @Test
        @DisplayName("Given valid UUID when creating RoleId then creates successfully")
        void givenValidUuid_whenCreatingRoleId_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();

            // When
            var roleId = new RoleId(validUuid);

            // Then
            assertThat(roleId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("Given valid UUID when using factory method then creates successfully")
        void givenValidUuid_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();

            // When
            var roleId = RoleId.of(validUuid);

            // Then
            assertThat(roleId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("Given valid UUID string when using factory method then creates successfully")
        void givenValidUuidString_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();
            var validUuidString = validUuid.toString();

            // When
            var roleId = RoleId.of(validUuidString);

            // Then
            assertThat(roleId.value()).isEqualTo(validUuid);
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given multiple RoleIds when comparing then they are distinct")
        void givenMultipleRoleIds_whenComparing_thenTheyAreDistinct() {
            // Given
            var firstRoleId = RoleId.of(UUID.randomUUID());
            var secondRoleId = RoleId.of(UUID.randomUUID());

            // Then
            assertThat(firstRoleId).isNotEqualTo(secondRoleId);
        }
    }

    @Nested
    @DisplayName("Boundary behaviors")
    class BoundaryBehavior {
        // No specific boundary tests for UUID since it has a fixed format and size
    }

    @Nested
    @DisplayName("Object contract compliance")
    class ObjectContractBehavior {
        @Test
        @DisplayName("Given same UUID when comparing RoleIds then they are equal")
        void givenSameUuid_whenComparingRoleIds_thenTheyAreEqual() {
            // Given
            var sharedUuid = UUID.randomUUID();
            var firstRoleId = RoleId.of(sharedUuid);
            var secondRoleId = RoleId.of(sharedUuid);

            // Then
            assertThat(firstRoleId).isEqualTo(secondRoleId).hasSameHashCodeAs(secondRoleId);
        }

        @Test
        @DisplayName("Given RoleId when calling toString then returns UUID string representation")
        void givenRoleId_whenCallingToString_thenReturnsUuidStringRepresentation() {
            // Given
            var uuid = UUID.randomUUID();
            var roleId = RoleId.of(uuid);

            // When
            var stringRepresentation = roleId.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo(uuid.toString());
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @Test
        @DisplayName("Given invalid UUID string when creating RoleId then throws InvalidRoleIdException")
        void givenInvalidUuidString_whenCreatingRoleId_thenThrowsInvalidRoleIdException() {
            // Given
            var malformedUuidString = "not-a-uuid";

            // Then
            assertThatThrownBy(() -> RoleId.of(malformedUuidString))
                .isInstanceOf(InvalidRoleIdException.class)
                .hasMessageContaining(malformedUuidString);
        }

        @Test
        @DisplayName("Given empty string when creating RoleId then throws InvalidRoleIdException")
        void givenEmptyString_whenCreatingRoleId_thenThrowsInvalidRoleIdException() {
            // Given
            var emptyUuidString = "";

            // Then
            assertThatThrownBy(() -> RoleId.of(emptyUuidString))
                .isInstanceOf(InvalidRoleIdException.class)
                .hasMessageContaining(emptyUuidString);
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given known UUID string when creating RoleId then value matches expected")
        void givenKnownUuidString_whenCreatingRoleId_thenValueMatchesExpected() {
            // Given
            var knownUuidString = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
            var expectedUuid = UUID.fromString(knownUuidString);

            // When
            var roleId = RoleId.of(knownUuidString);

            // Then
            assertThat(roleId.value()).isEqualTo(expectedUuid);
        }
    }
}
