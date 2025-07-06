package engineer.mkitsoukou.tika.application.auth.port.in;

import engineer.mkitsoukou.tika.application.auth.command.RegisterUserCommand;
import engineer.mkitsoukou.tika.application.auth.dto.UserDto;
import engineer.mkitsoukou.tika.application.shared.UseCase;

public interface RegisterUserUseCase
  extends UseCase<RegisterUserCommand, UserDto> {}
