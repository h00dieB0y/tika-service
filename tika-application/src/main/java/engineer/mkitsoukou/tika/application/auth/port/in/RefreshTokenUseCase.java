package engineer.mkitsoukou.tika.application.auth.port.in;

import engineer.mkitsoukou.tika.application.auth.command.RefreshTokenCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.shared.UseCase;

public interface RefreshTokenUseCase extends UseCase<RefreshTokenCommand, AuthTokensDto> {
}
