package ru.alspace.common.model.command;

import java.io.Serial;

public record LoginCommand(String userName, String clientType) implements Command {
    @Serial
    private static final long serialVersionUID = 1L;
}
