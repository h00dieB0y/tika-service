package engineer.mkitsoukou.tika.application.auth.port.out;

import engineer.mkitsoukou.tika.application.auth.exception.TooManyAttemptsException;

public interface RateLimiterPort {

  /**
   * Checks if the user is allowed to log in based on their email.
   * This method should be called before attempting to log in.
   * If the user has exceeded their login quota,
   * it throws a {@link TooManyAttemptsException}.
   *
   * @param email the user's email address
   * @throws TooManyAttemptsException if the client exceeded the login quota.
   */
  void checkLoginAllowed(String email);
  void recordSuccessfulLogin(String email);
}
