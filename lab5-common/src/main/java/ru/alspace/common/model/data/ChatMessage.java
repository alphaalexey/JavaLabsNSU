package ru.alspace.common.model.data;

import java.io.Serializable;
import java.time.Instant;

public record ChatMessage(String fromUser, String text, Instant timestamp) implements Serializable {
}
