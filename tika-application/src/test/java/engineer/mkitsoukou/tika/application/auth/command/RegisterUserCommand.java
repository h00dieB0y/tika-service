package engineer.mkitsoukou.tika.application.auth.command;

import engineer.mkitsoukou.tika.application.shared.Command;

public record RegisterUserCommand(String email, String password) implements Command {
}
