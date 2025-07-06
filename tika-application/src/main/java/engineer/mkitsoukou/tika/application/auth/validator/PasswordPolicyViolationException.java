package engineer.mkitsoukou.tika.application.auth.validator;

import java.util.List;

/**
 * Thrown when a password fails one or more {@link PasswordPolicyRule}s.
 * Carries the full list of breaches for API / UI feedback.
 */
public final class PasswordPolicyViolationException extends RuntimeException {

  private final List<PasswordPolicyViolation> violations;

  public PasswordPolicyViolationException(List<PasswordPolicyViolation> violations) {
    super("Password policy violated");
    this.violations = List.copyOf(violations);
  }

  public List<PasswordPolicyViolation> violations() {
    return violations;
  }
}
