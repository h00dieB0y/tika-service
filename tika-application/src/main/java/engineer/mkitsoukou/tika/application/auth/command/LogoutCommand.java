package engineer.mkitsoukou.tika.application.auth.command;

import engineer.mkitsoukou.tika.application.shared.Command;

public record LogoutCommand(String accessToken) implements Command {}

