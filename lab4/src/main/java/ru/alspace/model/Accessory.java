package ru.alspace.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Accessory extends Part {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Accessory() {
        super(counter.incrementAndGet());
    }
}
