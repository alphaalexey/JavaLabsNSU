package ru.alspace.common.model.response;

import java.io.Serial;

public record ErrorResponse(String message) implements Response {
    @Serial
    private static final long serialVersionUID = 1L;
}
