package engineer.mkitsoukou.tika.application.auth.validator;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DictionaryRuleTest {

  private final DictionaryRule rule = new DictionaryRule();

  /* S – Simple (strong password) */
  @Test
  void highEntropyPasswordPasses() {
    PasswordPolicyViolation v = rule.check("V3ry$trongAndUn1qu3Pwd");
    assertThat(v.isViolation()).isFalse();
  }

  /* E – Exceptions (weak password) */
  @Test
  void dictionaryPasswordFails() {
    PasswordPolicyViolation v = rule.check("Password1");
    assertThat(v.isViolation()).isTrue();
    assertThat(v.code()).isEqualTo("ENTROPY");
  }
}
