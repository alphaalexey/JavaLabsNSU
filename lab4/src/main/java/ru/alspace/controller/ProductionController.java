package ru.alspace.controller;

import ru.alspace.assembler.CarAssemblyTask;
import ru.alspace.model.Accessory;
import ru.alspace.model.Body;
import ru.alspace.model.Car;
import ru.alspace.model.Motor;
import ru.alspace.storage.Storage;
import ru.alspace.threadpool.ThreadPool;

public class ProductionController extends Thread {
    private final ThreadPool threadPool;
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private volatile boolean running = true;
    // Целевой уровень запасов готовых автомобилей (например, 10% от вместимости, минимум 1)
    private final int targetLevel;

    public ProductionController(ThreadPool threadPool, Storage<Body> bodyStorage,
                                Storage<Motor> motorStorage, Storage<Accessory> accessoryStorage,
                                Storage<Car> carStorage) {
        this.threadPool = threadPool;
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.targetLevel = Math.max(1, carStorage.getCapacity() / 10);
    }

    // Вызывается дилерами после продажи автомобиля
    public synchronized void notifySale() {
        notify();
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Ждем уведомление от дилеров или таймаут в 1 сек.
            synchronized (this) {
                try {
                    wait(1000); // пробуждение каждые 1000 мс
                } catch (InterruptedException e) {
                    break;
                }
            }
            // Если запас готовых машин ниже целевого, добавляем задачи сборки
            while (carStorage.size() < targetLevel) {
                threadPool.submit(new CarAssemblyTask(bodyStorage, motorStorage, accessoryStorage, carStorage));
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
