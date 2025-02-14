package ru.alspace.supplier;

import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class PartSupplier<T> extends Thread {
    protected final Storage<T> storage;
    public long delay; // задержка между поставками (меняется ползунками)
    protected volatile boolean running = true;
    protected final AtomicInteger producedCount;

    public PartSupplier(Storage<T> storage, long delay, AtomicInteger producedCount) {
        this.storage = storage;
        this.delay = delay;
        this.producedCount = producedCount;
    }

    protected abstract T createPart();

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                T part = createPart();
                storage.put(part);
                producedCount.incrementAndGet();
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
