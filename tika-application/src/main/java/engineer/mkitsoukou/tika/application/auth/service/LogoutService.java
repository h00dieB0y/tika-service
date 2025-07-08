package engineer.mkitsoukou.tika.application.auth.service;

import engineer.mkitsoukou.tika.application.auth.command.LogoutCommand;
import engineer.mkitsoukou.tika.application.auth.port.in.LogoutUseCase;
import engineer.mkitsoukou.tika.application.auth.port.out.JwtValidatorPort;
import engineer.mkitsoukou.tika.application.auth.port.out.RefreshTokenStorePort;
import engineer.mkitsoukou.tika.application.auth.port.out.TokenBlacklistPort;

public class LogoutService implements LogoutUseCase {
  private final JwtValidatorPort validator;
  private final TokenBlacklistPort blacklist;
  private final RefreshTokenStorePort rtStore;

  public LogoutService(
    JwtValidatorPort validator,
    TokenBlacklistPort blacklist,
    RefreshTokenStorePort rtStore
  ) {
    this.validator = validator;
    this.blacklist = blacklist;
    this.rtStore = rtStore;
  }

  @Override
  public Void execute(LogoutCommand command) {

    // Validate the access token to obtain the claims
    var claims = validator.validateAccessToken(command.accessToken());

    // Blacklist the JTI (JWT ID) from the token
    blacklist.blacklist(claims.jti());

    // Revoke all refresh tokens for the user
    rtStore.revoke(claims.userId(), null);

    return null;
  }
}
