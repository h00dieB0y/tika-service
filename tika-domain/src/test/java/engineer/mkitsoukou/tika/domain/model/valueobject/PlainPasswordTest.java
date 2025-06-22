package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("PlainPassword value object")
class PlainPasswordTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null password when creating PlainPassword then throws InvalidPasswordException")
        void givenNullPassword_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> new PlainPassword(null))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must not be null or blank");
        }

        @Test
        @DisplayName("Given null password when using factory method then throws InvalidPasswordException")
        void givenNullPassword_whenUsingFactoryMethod_thenThrowsInvalidPasswordException() {
            String nullPassword = null;
            assertThatThrownBy(() -> PlainPassword.of(nullPassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidInstanceCreation {
        @Test
        @DisplayName("Given valid password when creating PlainPassword then creates successfully")
        void givenValidPassword_whenCreatingPlainPassword_thenCreatesSuccessfully() {
            // Given
            var validPassword = "Password1!";

            // When
            var plainPassword = new PlainPassword(validPassword);

            // Then
            assertThat(plainPassword.clearText()).isEqualTo(validPassword);
        }

        @Test
        @DisplayName("Given valid password when using factory method then creates successfully")
        void givenValidPassword_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validPassword = "Password1!";

            // When
            var plainPassword = PlainPassword.of(validPassword);

            // Then
            assertThat(plainPassword.clearText()).isEqualTo(validPassword);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Password1!",
            "Abcdefg1!",
            "PASSword123$",
            "Complex@Password123",
            "P@ssw0rd!",
            "1Password!",
            "!Aa1Bb2Cc3"
        })
        @DisplayName("Given various valid passwords when creating PlainPassword then creates successfully")
        void givenVariousValidPasswords_whenCreatingPlainPassword_thenCreatesSuccessfully(String validPassword) {
            assertDoesNotThrow(() -> PlainPassword.of(validPassword));
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given different passwords when comparing then they are not equal")
        void givenDifferentPasswords_whenComparing_thenTheyAreNotEqual() {
            // Given
            var firstPassword = PlainPassword.of("Password1!");
            var secondPassword = PlainPassword.of("DifferentPass2@");

            // Then
            assertThat(firstPassword).isNotEqualTo(secondPassword);
        }
    }

    @Nested
    @DisplayName("Boundary behaviors")
    class BoundaryBehavior {
        @Test
        @DisplayName("Given minimum length password when creating PlainPassword then creates successfully")
        void givenMinimumLengthPassword_whenCreatingPlainPassword_thenCreatesSuccessfully() {
            // Given - 8 characters is the minimum
            var minLengthPassword = "Abcd1!@#";

            // Then
            assertDoesNotThrow(() -> PlainPassword.of(minLengthPassword));
        }

        @Test
        @DisplayName("Given maximum length password when creating PlainPassword then creates successfully")
        void givenMaximumLengthPassword_whenCreatingPlainPassword_thenCreatesSuccessfully() {
            // Given - 64 characters is the maximum
            var uppercase = "A";
            var lowercase = "b";
            var digit = "1";
            var special = "!";
            var filler = "x".repeat(60);
            var maxLengthPassword = uppercase + lowercase + digit + special + filler;

            // Then
            assertDoesNotThrow(() -> PlainPassword.of(maxLengthPassword));
        }

        @Test
        @DisplayName("Given password below minimum length when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordBelowMinimumLength_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            // Given - 7 characters is below minimum
            var tooShortPassword = "Abc1!@#";

            // Then
            assertThatThrownBy(() -> PlainPassword.of(tooShortPassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must be between 8 and 64 characters long");
        }

        @Test
        @DisplayName("Given password above maximum length when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordAboveMaximumLength_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            // Given - 65 characters is above maximum
            var uppercase = "A";
            var lowercase = "b";
            var digit = "1";
            var special = "!";
            var filler = "x".repeat(61);
            var tooLongPassword = uppercase + lowercase + digit + special + filler;

            // Then
            assertThatThrownBy(() -> PlainPassword.of(tooLongPassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must be between 8 and 64 characters long");
        }
    }

    @Nested
    @DisplayName("Object contract compliance")
    class ObjectContractBehavior {
        @Test
        @DisplayName("Given same password when comparing PlainPasswords then they are equal")
        void givenSamePassword_whenComparingPlainPasswords_thenTheyAreEqual() {
            // Given
            var passwordValue = "Password1!";
            var firstPlainPassword = PlainPassword.of(passwordValue);
            var secondPlainPassword = PlainPassword.of(passwordValue);

            // Then
            assertThat(firstPlainPassword).isEqualTo(secondPlainPassword)
                .hasSameHashCodeAs(passwordValue);
        }

        @Test
        @DisplayName("Given PlainPassword when calling toString then returns password text")
        void givenPlainPassword_whenCallingToString_thenReturnsPasswordText() {
            // Given
            var passwordValue = "Password1!";
            var plainPassword = PlainPassword.of(passwordValue);

            // When
            var stringRepresentation = plainPassword.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo(passwordValue);
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @Test
        @DisplayName("Given empty string when creating PlainPassword then throws InvalidPasswordException")
        void givenEmptyString_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of(""))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must not be null or blank");
        }

        @Test
        @DisplayName("Given blank string when creating PlainPassword then throws InvalidPasswordException")
        void givenBlankString_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of("  "))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must not be null or blank");
        }

        @Test
        @DisplayName("Given password without uppercase when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordWithoutUppercase_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of("password1!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must contain at least one uppercase letter");
        }

        @Test
        @DisplayName("Given password without lowercase when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordWithoutLowercase_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of("PASSWORD1!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must contain at least one lowercase letter");
        }

        @Test
        @DisplayName("Given password without digit when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordWithoutDigit_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of("Password!"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must contain at least one digit");
        }

        @Test
        @DisplayName("Given password without special character when creating PlainPassword then throws InvalidPasswordException")
        void givenPasswordWithoutSpecialCharacter_whenCreatingPlainPassword_thenThrowsInvalidPasswordException() {
            assertThatThrownBy(() -> PlainPassword.of("Password1"))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("must contain at least one special character");
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given password with whitespace when creating PlainPassword then trims whitespace")
        void givenPasswordWithWhitespace_whenCreatingPlainPassword_thenTrimsWhitespace() {
            // Given
            var passwordWithWhitespace = "  Password1!  ";
            var expectedPassword = "Password1!";

            // When
            var plainPassword = PlainPassword.of(passwordWithWhitespace);

            // Then
            assertThat(plainPassword.clearText()).isEqualTo(expectedPassword);
        }
    }
}
