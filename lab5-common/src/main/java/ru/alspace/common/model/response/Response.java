package ru.alspace.common.model.response;

import java.io.Serializable;

/**
 * Marker interface for server→client responses.
 */
public sealed interface Response extends Serializable
        permits SuccessResponse, ErrorResponse, UserListResponse, HistoryResponse {
}
