package engineer.mkitsoukou.tika.application.auth.validator;

import com.nulabinc.zxcvbn.Zxcvbn;

/**
 * Uses <a href="https://github.com/nulab/zxcvbn4j">zxcvbn-java</a> to reject
 * weak / dictionary passwords. Passes when score ≥ 3 (out of 4).
 */
public final class DictionaryRule implements PasswordPolicyRule {

  private static final int    MIN_STRENGTH = 3;    // 0..4
  private static final Zxcvbn ZXCVBN       = new Zxcvbn();   // thread-safe singleton

  @Override
  public PasswordPolicyViolation check(String pwd) {
    int score = ZXCVBN.measure(pwd).getScore();
    return score < MIN_STRENGTH
        ? new PasswordPolicyViolation("ENTROPY",
        "Password too weak: entropy score %d/4".formatted(score))
        : PasswordPolicyViolation.OK;
  }
}
