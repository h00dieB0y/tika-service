package engineer.mkitsoukou.tika.application.auth.port.out;

import engineer.mkitsoukou.tika.application.auth.model.JwtClaims;

public interface JwtValidatorPort {
  JwtClaims validateRefreshToken(String refreshToken);
}
