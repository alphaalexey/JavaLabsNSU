package ru.alspace.model;

import java.time.Duration;

public record ScoreRecord(int score, TetrisDifficulty difficulty, long gameTimeInMillis) {
    @Override
    public String toString() {
        Duration duration = Duration.ofMillis(gameTimeInMillis);
        return score + " (" + difficulty.getName() + ", " +
                String.format("%02d", duration.toHours()) + ':' +
                String.format("%02d", duration.toMinutesPart()) + ':' +
                String.format("%02d", duration.toSecondsPart()) + '.' +
                String.format("%03d", duration.toMillisPart()) + ')';
    }
}
