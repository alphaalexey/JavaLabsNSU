package ru.alspace.model;

import java.time.Duration;

public record ScoreRecord(int score, TetrisDifficulty difficulty, long gameTimeInMillis) {
    @Override
    public String toString() {
        Duration duration = Duration.ofMillis(gameTimeInMillis);
        return score + " (" + difficulty.getName() + ", " +
                duration.toHours() + ':' + duration.toMinutesPart() + ':' + duration.toSecondsPart() + '.' + duration.toMillisPart() + ')';
    }
}
