package engineer.mkitsoukou.tika.application.auth.validator;

/**
 * A single password-strength breach.
 *
 * @param code    machine-readable key (e.g. ENTROPY, REPEAT)
 * @param message human-readable explanation
 */
public record PasswordPolicyViolation(String code, String message) {

  /**
   * @return true if this violation is not empty, i.e. it has a code.
   */
  public boolean isViolation() {
    return code != null;
  }

  static final PasswordPolicyViolation OK = new PasswordPolicyViolation(null, null);
}
