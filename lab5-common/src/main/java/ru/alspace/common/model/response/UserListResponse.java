package ru.alspace.common.model.response;

import java.util.List;

public record UserListResponse(List<String> users) implements Response {
}
