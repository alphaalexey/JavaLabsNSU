package ru.alspace.common.model.event;

public record UserLogoutEvent(String userName) implements Event {
}
