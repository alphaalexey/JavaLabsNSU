package ru.alspace.storage;

import java.util.LinkedList;
import java.util.Queue;

public class Storage<T> {
    private final Queue<T> items = new LinkedList<>();
    private final int capacity;

    public Storage(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void put(T item) throws InterruptedException {
        while (items.size() >= capacity) {
            wait();
        }
        items.add(item);
        notifyAll();
    }

    public synchronized T get() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        T item = items.poll();
        notifyAll();
        return item;
    }

    public synchronized int size() {
        return items.size();
    }

    public int getCapacity() {
        return capacity;
    }
}
