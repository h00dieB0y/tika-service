package engineer.mkitsoukou.tika.application.auth.port.in;

import engineer.mkitsoukou.tika.application.auth.command.LogoutCommand;
import engineer.mkitsoukou.tika.application.shared.UseCase;

public interface LogoutUseCase extends UseCase<LogoutCommand, Void> {}
