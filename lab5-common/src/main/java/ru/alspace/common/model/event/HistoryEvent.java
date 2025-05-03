package ru.alspace.common.model.event;

import ru.alspace.common.model.data.ChatMessage;

import java.util.List;

public record HistoryEvent(List<ChatMessage> history) implements Event {
}
