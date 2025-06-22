package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidUserIdException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UserId value object")
class UserIdTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null UUID when creating UserId then throws NullPointerException")
        void givenNullUuid_whenCreatingUserId_thenThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () -> new UserId(null));
        }

        @Test
        @DisplayName("Given null UUID when using factory method then throws NullPointerException")
        void givenNullUuid_whenUsingFactoryMethod_thenThrowsNullPointerException() {
            UUID nullUuid = null;
            assertThrows(NullPointerException.class, () -> UserId.of(nullUuid));
        }

        @Test
        @DisplayName("Given null string when using factory method then throws NullPointerException")
        void givenNullString_whenUsingFactoryMethod_thenThrowsNullPointerException() {
            String nullString = null;
            assertThrows(NullPointerException.class, () -> UserId.of(nullString));
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidSingleInstanceCreation {
        @Test
        @DisplayName("Given valid UUID when creating UserId then creates successfully")
        void givenValidUuid_whenCreatingUserId_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();

            // When
            var userId = new UserId(validUuid);

            // Then
            assertThat(userId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("Given valid UUID when using factory method then creates successfully")
        void givenValidUuid_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();

            // When
            var userId = UserId.of(validUuid);

            // Then
            assertThat(userId.value()).isEqualTo(validUuid);
        }

        @Test
        @DisplayName("Given valid UUID string when using factory method then creates successfully")
        void givenValidUuidString_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validUuid = UUID.randomUUID();
            var validUuidString = validUuid.toString();

            // When
            var userId = UserId.of(validUuidString);

            // Then
            assertThat(userId.value()).isEqualTo(validUuid);
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given multiple UserIds when comparing then they are distinct")
        void givenMultipleUserIds_whenComparing_thenTheyAreDistinct() {
            // Given
            var firstUserId = UserId.of(UUID.randomUUID());
            var secondUserId = UserId.of(UUID.randomUUID());

            // Then
            assertThat(firstUserId).isNotEqualTo(secondUserId);
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
        @DisplayName("Given same UUID when comparing UserIds then they are equal")
        void givenSameUuid_whenComparingUserIds_thenTheyAreEqual() {
            // Given
            var sharedUuid = UUID.randomUUID();
            var firstUserId = UserId.of(sharedUuid);
            var secondUserId = UserId.of(sharedUuid);

            // Then
          assertThat(firstUserId)
            .isEqualTo(secondUserId)
            .hasSameHashCodeAs(secondUserId);
        }

        @Test
        @DisplayName("Given UserId when calling toString then returns UUID string representation")
        void givenUserId_whenCallingToString_thenReturnsUuidStringRepresentation() {
            // Given
            var uuid = UUID.randomUUID();
            var userId = UserId.of(uuid);

            // When
            var stringRepresentation = userId.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo(uuid.toString());
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @Test
        @DisplayName("Given invalid UUID string when creating UserId then throws InvalidUserIdException")
        void givenInvalidUuidString_whenCreatingUserId_thenThrowsInvalidUserIdException() {
            // Given
            var malformedUuidString = "not-a-uuid";

            // Then
            assertThatThrownBy(() -> UserId.of(malformedUuidString))
                .isInstanceOf(InvalidUserIdException.class)
                .hasMessageContaining(malformedUuidString);
        }

        @Test
        @DisplayName("Given empty string when creating UserId then throws InvalidUserIdException")
        void givenEmptyString_whenCreatingUserId_thenThrowsInvalidUserIdException() {
            // Given
            var emptyUuidString = "";

            // Then
            assertThatThrownBy(() -> UserId.of(emptyUuidString))
                .isInstanceOf(InvalidUserIdException.class)
                .hasMessageContaining(emptyUuidString);
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given known UUID string when creating UserId then value matches expected")
        void givenKnownUuidString_whenCreatingUserId_thenValueMatchesExpected() {
            // Given
            var knownUuidString = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
            var expectedUuid = UUID.fromString(knownUuidString);

            // When
            var userId = UserId.of(knownUuidString);

            // Then
            assertThat(userId.value()).isEqualTo(expectedUuid);
        }
    }
}
