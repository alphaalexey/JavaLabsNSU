package ru.alspace.common.model.event;

public record UserLoginEvent(String userName) implements Event {
}
