package ru.alspace.model;

public abstract class Part {
    protected final int id;

    public Part(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
