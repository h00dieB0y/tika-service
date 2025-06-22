package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("PasswordHash value object")
class PasswordHashTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null hash when creating PasswordHash then throws InvalidPasswordException")
        void givenNullHash_whenCreatingPasswordHash_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> new PasswordHash(null))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password hash must not be null or blank");
        }

        @Test
        @DisplayName("Given null hash when using factory method then throws InvalidPasswordException")
        void givenNullHash_whenUsingFactoryMethod_thenThrowsInvalidPasswordException() {
            String nullHash = null;
            assertThatThrownBy(() -> PasswordHash.of(nullHash))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password hash must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidInstanceCreation {
        @Test
        @DisplayName("Given valid hash when creating PasswordHash then creates successfully")
        void givenValidHash_whenCreatingPasswordHash_thenCreatesSuccessfully() {
            // Given
            var validHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

            // When
            var passwordHash = new PasswordHash(validHash);

            // Then
            assertThat(passwordHash.hash()).isEqualTo(validHash);
        }

        @Test
        @DisplayName("Given valid hash when using factory method then creates successfully")
        void givenValidHash_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

            // When
            var passwordHash = PasswordHash.of(validHash);

            // Then
            assertThat(passwordHash.hash()).isEqualTo(validHash);
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given different hashes when comparing then they are not equal")
        void givenDifferentHashes_whenComparing_thenTheyAreNotEqual() {
            // Given
            var firstHash = PasswordHash.of("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
            var secondHash = PasswordHash.of("$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghijklmnopqrstuv");

            // Then
            assertThat(firstHash).isNotEqualTo(secondHash);
        }
    }

    @Nested
    @DisplayName("Boundary behaviors")
    class BoundaryBehavior {
        @Test
        @DisplayName("Given hash with minimal length when creating PasswordHash then validates successfully")
        void givenHashWithMinimalLength_whenCreatingPasswordHash_thenValidatesSuccessfully() {
            // Assuming minimum length is defined (adjust based on actual implementation)
            var minimalHash = "$2a$10$" + "a".repeat(53); // 60 chars for BCrypt

            assertDoesNotThrow(() -> PasswordHash.of(minimalHash));
        }
    }

    @Nested
    @DisplayName("Object contract compliance")
    class ObjectContractBehavior {
        @Test
        @DisplayName("Given same hash when comparing PasswordHashes then they are equal")
        void givenSameHash_whenComparingPasswordHashes_thenTheyAreEqual() {
            // Given
            var hashValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
            var firstPasswordHash = PasswordHash.of(hashValue);
            var secondPasswordHash = PasswordHash.of(hashValue);

            // Then
            assertThat(firstPasswordHash).isEqualTo(secondPasswordHash);
            assertThat(firstPasswordHash.hashCode()).isEqualTo(secondPasswordHash.hashCode());
        }

        @Test
        @DisplayName("Given PasswordHash when calling toString then returns masked representation")
        void givenPasswordHash_whenCallingToString_thenReturnsMaskedRepresentation() {
            // Given
            var hashValue = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
            var passwordHash = PasswordHash.of(hashValue);

            // When
            var stringRepresentation = passwordHash.toString();

            // Then - assuming implementation masks the hash for security
            assertThat(stringRepresentation).doesNotContain(hashValue);
            assertThat(stringRepresentation).contains("[PROTECTED]");
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @Test
        @DisplayName("Given empty string when creating PasswordHash then throws InvalidPasswordException")
        void givenEmptyString_whenCreatingPasswordHash_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PasswordHash.of(""))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password hash must not be null or blank");
        }

        @Test
        @DisplayName("Given blank string when creating PasswordHash then throws InvalidPasswordException")
        void givenBlankString_whenCreatingPasswordHash_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PasswordHash.of("  "))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Password hash must not be null or blank");
        }

        @Test
        @DisplayName("Given invalid hash format when creating PasswordHash then throws InvalidPasswordException")
        void givenInvalidHashFormat_whenCreatingPasswordHash_thenThrowsInvalidPasswordException() {
            // Assuming the implementation validates hash format
            var invalidHash = "not-a-valid-hash-format";

            assertThatThrownBy(() -> PasswordHash.of(invalidHash))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid password hash format");
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given hash with whitespace when creating PasswordHash then trims whitespace")
        void givenHashWithWhitespace_whenCreatingPasswordHash_thenTrimsWhitespace() {
            // Given
            var hashWithWhitespace = "  $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy  ";
            var expectedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

            // When
            var passwordHash = PasswordHash.of(hashWithWhitespace);

            // Then
            assertThat(passwordHash.hash()).isEqualTo(expectedHash);
        }
    }
}
