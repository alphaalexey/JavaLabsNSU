package ru.alspace.assembler;

import ru.alspace.model.Accessory;
import ru.alspace.model.Body;
import ru.alspace.model.Car;
import ru.alspace.model.Motor;
import ru.alspace.storage.Storage;

import java.util.concurrent.atomic.AtomicInteger;

public class CarAssemblyTask implements Runnable {
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Car> carStorage;
    private final AtomicInteger pendingAssemblyTasks;

    public CarAssemblyTask(Storage<Body> bodyStorage, Storage<Motor> motorStorage,
                           Storage<Accessory> accessoryStorage, Storage<Car> carStorage,
                           AtomicInteger pendingAssemblyTasks) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.carStorage = carStorage;
        this.pendingAssemblyTasks = pendingAssemblyTasks;
    }

    @Override
    public void run() {
        try {
            // Извлекаем необходимые детали (если их нет, поток ждёт)
            Body body = bodyStorage.get();
            Motor motor = motorStorage.get();
            Accessory accessory = accessoryStorage.get();
            // Собираем автомобиль
            Car car = new Car(body, motor, accessory);
            // Отправляем готовый автомобиль на склад
            carStorage.put(car);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // После завершения сборки уменьшаем счётчик ожидающих задач
            pendingAssemblyTasks.decrementAndGet();
        }
    }
}
