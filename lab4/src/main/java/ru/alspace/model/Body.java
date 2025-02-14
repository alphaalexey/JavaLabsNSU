package ru.alspace.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Body extends Part {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Body() {
        super(counter.incrementAndGet());
    }
}
