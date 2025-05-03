package ru.alspace.common.model.command;

public record LoginCommand(String userName, String clientType) implements Command {
}
