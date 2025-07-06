package engineer.mkitsoukou.tika.application.auth.validator;

/**
 * Fails when the same character repeats more than {@code maxRepeat} times in a row.
 */
public record RepeatedCharRule(int maxRepeat) implements PasswordPolicyRule {

  /**
   * Validates that no character in the password repeats more than the allowed maximum times.
   *
   * @param pwd The password to check
   * @return A PasswordPolicyViolation if the rule is violated, or PasswordPolicyViolation.OK otherwise
   */
  @Override
  public PasswordPolicyViolation check(String pwd) {
    if (pwd.isEmpty() || maxRepeat <= 0) {
      return PasswordPolicyViolation.OK;
    }

    int run = 1;
    char prevChar = pwd.charAt(0);

    for (int i = 1; i < pwd.length(); i++) {
      char currentChar = pwd.charAt(i);

      if (currentChar == prevChar) {
        run++;
        if (run > maxRepeat) {
          return new PasswordPolicyViolation(
              "REPEAT",
              "Character '%c' repeated %d times, exceeding the limit of %d".formatted(
                  currentChar, run, maxRepeat));
        }
      } else {
        run = 1;
        prevChar = currentChar;
      }
    }

    return PasswordPolicyViolation.OK;
  }
}
