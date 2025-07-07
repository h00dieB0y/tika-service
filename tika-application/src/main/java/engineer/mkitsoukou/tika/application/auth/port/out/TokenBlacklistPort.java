package engineer.mkitsoukou.tika.application.auth.port.out;

public interface TokenBlacklistPort {
  boolean isBlacklisted(String jti);
}
