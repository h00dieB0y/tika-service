package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.RefreshTokenCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.auth.exception.InvalidCredentialsException;
import engineer.mkitsoukou.tika.application.auth.model.AuthSubject;
import engineer.mkitsoukou.tika.application.auth.model.JwtClaims;
import engineer.mkitsoukou.tika.application.auth.port.in.RefreshTokenUseCase;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtIssuerPort;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtValidatorPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RefreshTokenStorePort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;
import engineer.mkitsoukou.tika.application.shared.ClockPort;

public class RefreshTokenService implements RefreshTokenUseCase {
  private final JwtValidatorPort validator;
  private final JwtIssuerPort issuer;
  private final RefreshTokenStorePort rtStore;
  private final TokenBlacklistPort blacklist;
  private final ClockPort clock;

  public RefreshTokenService(
      JwtValidatorPort validator,
      JwtIssuerPort issuer,
      RefreshTokenStorePort rtStore,
      TokenBlacklistPort blacklist,
      ClockPort clock
  ) {
    this.validator = validator;
    this.issuer = issuer;
    this.rtStore = rtStore;
    this.blacklist = blacklist;
    this.clock = clock;
  }

  @Override
  public AuthTokensDto execute(RefreshTokenCommand command) {
    // Validate the refresh token
    JwtClaims claims = validator.validateRefreshToken(command.refreshToken());

    // Check if the refresh token is still known and not blacklisted
    if (!rtStore.isValid(claims.userId(), command.refreshToken())) {
      throw new InvalidCredentialsException();
    }

    // Issue new tokens
    AuthSubject subject = new AuthSubject(claims.userId(), claims.roles());
    AuthTokensDto tokens = issuer.issueTokens(subject, clock.now());

    // Rotate RT: revoke the old, store the new
    rtStore.revoke(claims.userId(), command.refreshToken());
    rtStore.store(claims.userId(), tokens.refreshToken(), tokens.expiresAt());

    // Blacklist the old AT
    blacklist.blacklist(claims.jti());

    return tokens;
  }
}
