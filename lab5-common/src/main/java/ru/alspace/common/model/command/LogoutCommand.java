package ru.alspace.common.model.command;

import java.io.Serial;

public record LogoutCommand(String sessionId) implements Command {
    @Serial
    private static final long serialVersionUID = 1L;
}
