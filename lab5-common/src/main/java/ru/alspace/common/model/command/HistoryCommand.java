package ru.alspace.common.model.command;

public record HistoryCommand(String sessionId) implements Command {
}
