package ru.alspace.controller;

import ru.alspace.assembler.CarAssemblyTask;
import ru.alspace.model.Accessory;
import ru.alspace.model.Body;
import ru.alspace.model.Car;
import ru.alspace.model.Motor;
import ru.alspace.storage.Storage;
import ru.alspace.threadpool.ThreadPool;

import java.util.concurrent.atomic.AtomicInteger;

public class ProductionController extends Thread {
    private final ThreadPool threadPool;
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private volatile boolean running = true;

    // Счётчик задач сборки, которые были запущены, но ещё не завершились
    private final AtomicInteger pendingAssemblyTasks = new AtomicInteger(0);

    public ProductionController(ThreadPool threadPool, Storage<Body> bodyStorage,
                                Storage<Motor> motorStorage, Storage<Accessory> accessoryStorage,
                                Storage<Car> carStorage) {
        this.threadPool = threadPool;
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
    }

    // Метод для пробуждения контроллера, вызывается дилерами после продажи автомобиля
    public synchronized void notifySale() {
        notify();
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            // Сначала инициируем производство, если есть свободное место на складе
            int availableSpace = carStorage.getCapacity() - (carStorage.size() + pendingAssemblyTasks.get());
            while (availableSpace > 0 && running) {
                pendingAssemblyTasks.incrementAndGet();
                threadPool.submit(new CarAssemblyTask(bodyStorage, motorStorage, accessoryStorage, carStorage, pendingAssemblyTasks));
                availableSpace--;
            }
            // После запуска доступных задач переходим в режим ожидания уведомлений (например, о продаже)
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}
