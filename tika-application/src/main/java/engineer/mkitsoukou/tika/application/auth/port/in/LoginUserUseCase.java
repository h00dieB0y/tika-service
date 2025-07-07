package engineer.mkitsoukou.tika.application.auth.port.in;

import engineer.mkitsoukou.tika.application.auth.command.LoginUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.AuthTokensDto;
import engineer.mkitsoukou.tika.application.shared.UseCase;

public interface LoginUserUseCase extends UseCase<LoginUserCommand, AuthTokensDto> {
}
