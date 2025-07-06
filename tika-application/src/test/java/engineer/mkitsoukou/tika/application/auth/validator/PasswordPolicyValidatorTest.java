package engineer.mkitsoukou.tika.application.auth.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PasswordPolicyValidatorTest {

  private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

  /* O – One (happy-path) */
  @Test
  void strongPasswordShouldPass() {
    String strong = "Th1s1sAv3ryS3cur3Pwd!";   // entropy 4/4, no repeats
    assertThatCode(() -> validator.validate(strong))
      .doesNotThrowAnyException();
  }

  /* E – Exceptions  (weak entropy) */
  @Test
  void weakDictionaryPasswordShouldFail() {
    String weak = "Password123!";
    assertThatThrownBy(() -> validator.validate(weak))
      .isInstanceOf(PasswordPolicyViolationException.class)
      .satisfies(ex -> assertThat(((PasswordPolicyViolationException) ex)
        .violations()).anyMatch(v -> "ENTROPY".equals(v.code())));
  }

  /* M – Many  (repetition) */
  @Test
  void repeatedCharactersShouldFail() {
    String repeated = "AAAAAbbbb1111!!!!";   // 4+ identical chars in a row
    assertThatThrownBy(() -> validator.validate(repeated))
      .isInstanceOf(PasswordPolicyViolationException.class)
      .satisfies(ex -> assertThat(
        ((PasswordPolicyViolationException) ex).violations())
        .anyMatch(v -> "REPEAT".equals(v.code())));
  }

  /* Z – Zero / empty */
  @Test
  void emptyPasswordStillFailsBecauseEntropy() {
    assertThatThrownBy(() -> validator.validate(""))
      .isInstanceOf(PasswordPolicyViolationException.class);
  }

  /* I – Interface (custom rule set)  & S – Simple */
  @Test
  void customValidatorAcceptsWhenRulesPass() {
    PasswordPolicyRule noRules = pwd -> PasswordPolicyViolation.OK;   // always OK
    PasswordPolicyValidator relaxed = new PasswordPolicyValidator(
      java.util.List.of(noRules));

    assertThatCode(() -> relaxed.validate("tooWeak"))
      .doesNotThrowAnyException();
  }
}
