package engineer.mkitsoukou.tika.application.auth.port.out;

public interface RateLimiterPort {
  void checkLoginAllowed(String email);
  void recordSuccessfulLogin(String email);
}
