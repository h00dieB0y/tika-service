package engineer.mkitsoukou.tika.application.auth.validator;

/**
 * Fails when the same character repeats more than {@code maxRepeat} times in a row.
 */
public record RepeatedCharRule(int maxRepeat) implements PasswordPolicyRule {

  @Override
  public PasswordPolicyViolation check(String pwd) {
    int run = 1;
    int maxRun = 1;
    for (int i = 1; i < pwd.length(); i++) {
      run = (pwd.charAt(i) == pwd.charAt(i - 1)) ? run + 1 : 1;
      if (run > maxRepeat) {
        return new PasswordPolicyViolation("REPEAT",
            "Character repetition > %d not allowed".formatted(maxRepeat));
      }
      maxRun = Math.max(maxRun, run);
    }
    return PasswordPolicyViolation.OK;
  }
}
