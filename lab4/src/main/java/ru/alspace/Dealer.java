package ru.alspace;

import ru.alspace.controller.ProductionController;
import ru.alspace.model.Car;
import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public class Dealer extends Thread {
    private final int dealerId;
    private final Storage<Car> carStorage;
    private final FactoryLogger logger;
    private final AtomicInteger totalSales;
    private final ProductionController controller;
    public long delay; // задержка между запросами (изменяется через ползунок)
    private volatile boolean running = true;

    public Dealer(int id, Storage<Car> carStorage, long delay, FactoryLogger logger, AtomicInteger totalSales, ProductionController controller) {
        this.dealerId = id;
        this.carStorage = carStorage;
        this.delay = delay;
        this.logger = logger;
        this.totalSales = totalSales;
        this.controller = controller;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Car car = carStorage.get();
                // Запись в лог при продаже
                if (logger != null) {
                    logger.logSale(dealerId, car);
                }
                totalSales.incrementAndGet();
                // Уведомление контроллера для запуска задачи сборки новой машины
                controller.notifySale();
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
