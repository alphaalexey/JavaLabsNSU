package ru.alspace.common.model.event;

import java.io.Serializable;

/**
 * Marker interface for all chat events.
 */
public sealed interface Event extends Serializable
        permits MessageEvent, UserLoginEvent, UserLogoutEvent {
}
