package engineer.mkitsoukou.tika.application.auth.validator;

import java.util.List;

@FunctionalInterface
public interface PasswordPolicyRule {

  /**
   * @param pwd raw plain-text password (never {@code null})
   * @return an OK object if rule passes; otherwise a populated violation
   */
  PasswordPolicyViolation check(String pwd);

  /* ---------- Default rule-set ---------- */
  List<PasswordPolicyRule> DEFAULT_RULES = List.of(
    new RepeatedCharRule(4),
    new DictionaryRule()
  );
}
