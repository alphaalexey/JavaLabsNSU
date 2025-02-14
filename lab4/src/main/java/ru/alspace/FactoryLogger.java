package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.model.Car;

public record FactoryLogger(boolean enabled) {
    // Логгер для продаж, имя "SaleLogger" определяется в конфигурации log4j2
    private static final Logger logger = LogManager.getLogger("SaleLogger");

    public synchronized void logSale(int dealerId, Car car) {
        if (!enabled) return;
        logger.info("Dealer {}: Auto {} (Body: {}, Motor: {}, Accessory: {})", dealerId, car.getId(), car.getBody().getId(), car.getMotor().getId(), car.getAccessory().getId());
    }
}
