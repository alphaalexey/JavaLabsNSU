package ru.alspace.common.model.command;

public record LogoutCommand(String sessionId) implements Command {
}
