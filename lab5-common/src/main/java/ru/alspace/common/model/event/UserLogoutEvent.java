package ru.alspace.common.model.event;

import java.io.Serial;

public record UserLogoutEvent(String userName) implements Event {
    @Serial
    private static final long serialVersionUID = 1L;
}
