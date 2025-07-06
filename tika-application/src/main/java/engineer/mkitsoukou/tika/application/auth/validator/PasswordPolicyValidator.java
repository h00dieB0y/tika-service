package engineer.mkitsoukou.tika.application.auth.validator;

import java.util.List;

public final class PasswordPolicyValidator {

  private final List<PasswordPolicyRule> rules;

  /** Uses {@link PasswordPolicyRule#DEFAULT_RULES}. */
  public PasswordPolicyValidator() {
    this(PasswordPolicyRule.DEFAULT_RULES);
  }

  public PasswordPolicyValidator(List<PasswordPolicyRule> rules) {
    this.rules = List.copyOf(rules);
  }

  /**
   * Validate or throw {@link PasswordPolicyViolationException}.
   */
  public void validate(String plainPassword) {
    List<PasswordPolicyViolation> violations = rules.stream()
        .map(r -> r.check(plainPassword))
        .filter(PasswordPolicyViolation::isViolation)
        .toList();

    if (!violations.isEmpty()) {
      throw new PasswordPolicyViolationException(violations);
    }
  }
}
