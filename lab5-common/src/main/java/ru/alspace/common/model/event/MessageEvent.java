package ru.alspace.common.model.event;

public record MessageEvent(String fromUser, String text) implements Event {
}
