package ru.alspace.common.model.response;

import ru.alspace.common.model.data.ChatMessage;

import java.util.List;

public record HistoryResponse(List<ChatMessage> history) implements Response {
}
