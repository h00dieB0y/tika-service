package engineer.mkitsoukou.tika.domain.model.valueobject;

import engineer.mkitsoukou.tika.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Email value object")
class EmailTest {

    @Nested
    @DisplayName("Null value handling")
    class NullValueHandling {
        @Test
        @DisplayName("Given null email when creating Email then throws InvalidEmailException")
        void givenNullEmail_whenCreatingEmail_thenThrowsInvalidEmailException() {
            assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Email must not be null or blank");
        }

        @Test
        @DisplayName("Given null email when using factory method then throws InvalidEmailException")
        void givenNullEmail_whenUsingFactoryMethod_thenThrowsInvalidEmailException() {
            String nullEmail = null;
            assertThatThrownBy(() -> Email.of(nullEmail))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Email must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Valid instance creation")
    class ValidInstanceCreation {
        @Test
        @DisplayName("Given valid email when creating Email then creates successfully")
        void givenValidEmail_whenCreatingEmail_thenCreatesSuccessfully() {
            // Given
            var validEmail = "user@example.com";

            // When
            var email = new Email(validEmail);

            // Then
            assertThat(email.value()).isEqualTo(validEmail);
        }

        @Test
        @DisplayName("Given valid email when using factory method then creates successfully")
        void givenValidEmail_whenUsingFactoryMethod_thenCreatesSuccessfully() {
            // Given
            var validEmail = "user@example.com";

            // When
            var email = Email.of(validEmail);

            // Then
            assertThat(email.value()).isEqualTo(validEmail);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-hyphen@example.com",
            "fully-qualified-domain@example.com",
            "user.name+tag+sorting@example.com",
            "x@example.com",
            "example-indeed@strange-example.com",
            "example@s.example"
        })
        @DisplayName("Given various valid email formats when creating Email then creates successfully")
        void givenVariousValidEmailFormats_whenCreatingEmail_thenCreatesSuccessfully(String validEmail) {
            assertDoesNotThrow(() -> Email.of(validEmail));
        }
    }

    @Nested
    @DisplayName("Multiple instance behavior")
    class MultipleInstanceBehavior {
        @Test
        @DisplayName("Given different emails when comparing then they are not equal")
        void givenDifferentEmails_whenComparing_thenTheyAreNotEqual() {
            // Given
            var firstEmail = Email.of("user1@example.com");
            var secondEmail = Email.of("user2@example.com");

            // Then
            assertThat(firstEmail).isNotEqualTo(secondEmail);
        }
    }

    @Nested
    @DisplayName("Boundary behaviors")
    class BoundaryBehavior {
        @Test
        @DisplayName("Given email with minimal domain parts when creating Email then creates successfully")
        void givenEmailWithMinimalDomainParts_whenCreatingEmail_thenCreatesSuccessfully() {
            assertDoesNotThrow(() -> Email.of("user@x.yz"));
        }

        @Test
        @DisplayName("Given very long email when creating Email then creates successfully")
        void givenVeryLongEmail_whenCreatingEmail_thenCreatesSuccessfully() {
            // 64 characters in local part + @ + 255 character domain should be valid per RFC
            var longLocalPart = "a".repeat(64);
            var longDomainPart = String.join(".", "domain", "a".repeat(60), "com");
            var longEmail = longLocalPart + "@" + longDomainPart;

            assertDoesNotThrow(() -> Email.of(longEmail));
        }
    }

    @Nested
    @DisplayName("Object contract compliance")
    class ObjectContractBehavior {
        @Test
        @DisplayName("Given same email when comparing then they are equal")
        void givenSameEmail_whenComparing_thenTheyAreEqual() {
            // Given
            var emailValue = "same@example.com";
            var firstEmail = Email.of(emailValue);
            var secondEmail = Email.of(emailValue);

            // Then
            assertThat(firstEmail).isEqualTo(secondEmail).hasSameHashCodeAs(secondEmail);
        }

        @Test
        @DisplayName("Given Email when calling toString then returns email string")
        void givenEmail_whenCallingToString_thenReturnsEmailString() {
            // Given
            var emailValue = "user@example.com";
            var email = Email.of(emailValue);

            // When
            var stringRepresentation = email.toString();

            // Then
            assertThat(stringRepresentation).isEqualTo(emailValue);
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class InvalidInputHandling {
        @ParameterizedTest
        @ValueSource(strings = {
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com",
            "email@example.com (Joe Smith)",
            "email@example",
            "email@-example.com",
            "email@example..com"
        })
        @DisplayName("Given invalid email format when creating Email then throws InvalidEmailException")
        void givenInvalidEmailFormat_whenCreatingEmail_thenThrowsInvalidEmailException(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("must be a valid email format");
        }

        @Test
        @DisplayName("Given empty string when creating Email then throws InvalidEmailException")
        void givenEmptyString_whenCreatingEmail_thenThrowsInvalidEmailException() {
            assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Email must not be null or blank");
        }

        @Test
        @DisplayName("Given blank string when creating Email then throws InvalidEmailException")
        void givenBlankString_whenCreatingEmail_thenThrowsInvalidEmailException() {
            assertThatThrownBy(() -> Email.of("  "))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Email must not be null or blank");
        }
    }

    @Nested
    @DisplayName("Common use cases")
    class CommonUseCases {
        @Test
        @DisplayName("Given email with whitespace when creating Email then trims whitespace")
        void givenEmailWithWhitespace_whenCreatingEmail_thenTrimsWhitespace() {
            // Given
            var emailWithWhitespace = "  user@example.com  ";
            var expectedEmail = "user@example.com";

            // When
            var email = Email.of(emailWithWhitespace);

            // Then
            assertThat(email.value()).isEqualTo(expectedEmail);
        }
    }
}
