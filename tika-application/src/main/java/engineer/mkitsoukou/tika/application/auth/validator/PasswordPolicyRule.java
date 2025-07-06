package engineer.mkitsoukou.tika.application.auth.validator;

import java.util.List;

/**
 * Strategy for a single password rule.
 * Implementations are <strong>stateless</strong> and thread-safe.
 */
public sealed interface PasswordPolicyRule
  permits RepeatedCharRule, DictionaryRule {

  /**
   * @param pwd raw plain-text password (never {@code null})
   * @return an OK object if rule passes; otherwise a populated violation
   */
  PasswordPolicyViolation check(String pwd);

  /* ---------- Default compromise rule-set ---------- */
  List<PasswordPolicyRule> DEFAULT_RULES = List.of(
    new RepeatedCharRule(4),   // AAAA or 1111 not allowed for example
    new DictionaryRule()       // entropy score ≥ 3 via zxcvbn
  );
}
