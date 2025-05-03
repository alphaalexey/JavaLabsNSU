package ru.alspace.common.model.command;

public record MessageCommand(String sessionId, String text) implements Command {
}
