package engineer.mkitsoukou.tika.application.auth.validator;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class RepeatedCharRuleTest {

  private final RepeatedCharRule rule = new RepeatedCharRule(4);

  @Test
  void belowThresholdIsOk() {
    PasswordPolicyViolation v = rule.check("AaBbCc123!");
    assertThat(v.isViolation()).isFalse();
  }

  @Test
  void exactlyThresholdIsStillOk() {
    PasswordPolicyViolation v = rule.check("AAAA");   // 4 repeats == threshold
    assertThat(v.isViolation()).isFalse();
  }

  @Test
  void emptyStringIsOk() {
    assertThat(rule.check("").isViolation()).isFalse();
  }

  @Test
  void aboveThresholdProducesViolation() {
    PasswordPolicyViolation v = rule.check("AAAAA");  // 5 repeats
    assertThat(v).satisfies(p -> {
      assertThat(p.isViolation()).isTrue();
      assertThat(p.code()).isEqualTo("REPEAT");
    });
  }
}
