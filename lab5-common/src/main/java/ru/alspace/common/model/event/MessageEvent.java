package ru.alspace.common.model.event;

import java.io.Serial;

public record MessageEvent(String fromUser, String text) implements Event {
    @Serial
    private static final long serialVersionUID = 1L;
}
