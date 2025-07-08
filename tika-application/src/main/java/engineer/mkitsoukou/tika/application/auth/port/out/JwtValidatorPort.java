package engineer.mkitsoukou.tika.application.auth.port.out;

import engineer.mkitsoukou.tika.application.auth.model.JwtClaims;

public interface JwtValidatorPort {

  /**
   * Validates the access token and returns the claims if valid.
   *
   * @param accessToken the access token to validate
   * @return the JWT claims if the token is valid
   */
  JwtClaims validateAccessToken(String accessToken);

  /**
   * Validates the refresh token and returns the claims if valid.
   *
   * @param refreshToken the refresh token to validate
   * @return the JWT claims if the token is valid
   */
  JwtClaims validateRefreshToken(String refreshToken);
}
