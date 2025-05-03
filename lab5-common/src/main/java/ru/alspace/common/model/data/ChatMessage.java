package ru.alspace.common.model.data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Represents one chat message with timestamp.
 */
public record ChatMessage(
        String fromUser,
        String text,
        Instant timestamp
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
