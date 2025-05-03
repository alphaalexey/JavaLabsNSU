package ru.alspace.common.model.response;

import java.io.Serial;

public record SuccessResponse(String sessionId) implements Response {
    @Serial
    private static final long serialVersionUID = 1L;
}
