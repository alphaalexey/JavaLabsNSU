package ru.alspace.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Motor extends Part {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Motor() {
        super(counter.incrementAndGet());
    }
}
