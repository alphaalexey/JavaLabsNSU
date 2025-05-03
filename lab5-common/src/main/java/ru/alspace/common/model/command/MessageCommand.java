package ru.alspace.common.model.command;

import java.io.Serial;

public record MessageCommand(String sessionId, String text) implements Command {
    @Serial
    private static final long serialVersionUID = 1L;
}
