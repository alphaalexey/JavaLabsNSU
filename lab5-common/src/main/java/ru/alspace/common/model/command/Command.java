package ru.alspace.common.model.command;

import java.io.Serializable;

/**
 * Marker interface for client -> server commands.
 */
public sealed interface Command extends Serializable
        permits LoginCommand, ListCommand, HistoryCommand, MessageCommand, LogoutCommand {
}
