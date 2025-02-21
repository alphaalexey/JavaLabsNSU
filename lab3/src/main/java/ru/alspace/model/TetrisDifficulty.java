package ru.alspace.model;

public enum TetrisDifficulty {
    EASY("Easy", 600, 10, 300),
    NORMAL("Normal", 500, 15, 250),
    HARD("Hard", 400, 20, 150);

    private final String name;
    private final int initialDelay;
    private final int accelerationDelta;
    private final int minDelay;

    TetrisDifficulty(String name, int initialDelay, int accelerationDelta, int minDelay) {
        this.name = name;
        this.initialDelay = initialDelay;
        this.accelerationDelta = accelerationDelta;
        this.minDelay = minDelay;
    }

    public String getName() {
        return name;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public int getAccelerationDelta() {
        return accelerationDelta;
    }

    public int getMinDelay() {
        return minDelay;
    }

    @Override
    public String toString() {
        return name;
    }

    public static TetrisDifficulty getDefault() {
        return NORMAL;
    }
}
