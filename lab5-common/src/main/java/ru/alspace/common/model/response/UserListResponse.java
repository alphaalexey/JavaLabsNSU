package ru.alspace.common.model.response;

import java.io.Serial;
import java.util.List;

public record UserListResponse(List<String> users) implements Response {
    @Serial
    private static final long serialVersionUID = 1L;
}
