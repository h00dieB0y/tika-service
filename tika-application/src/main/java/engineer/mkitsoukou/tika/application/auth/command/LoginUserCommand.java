package engineer.mkitsoukou.tika.application.auth.command;

import engineer.mkitsoukou.tika.application.shared.Command;

public record LoginUserCommand(String email, String password) implements Command {
}
