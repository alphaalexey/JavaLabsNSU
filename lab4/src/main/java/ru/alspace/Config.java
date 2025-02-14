package ru.alspace;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    public int storageBodySize;
    public int storageMotorSize;
    public int storageAccessorySize;
    public int storageAutoSize;
    public int accessorySuppliers;
    public int workers;
    public int dealers;
    public boolean logSale;

    // Задержки (в мс) для поставщиков и дилеров
    public long bodySupplierDelay;
    public long motorSupplierDelay;
    public long accessorySupplierDelay;
    public long dealerDelay;

    public Config(String fileName) {
        // Значения по умолчанию
        storageBodySize = 100;
        storageMotorSize = 100;
        storageAccessorySize = 100;
        storageAutoSize = 100;
        accessorySuppliers = 5;
        workers = 10;
        dealers = 20;
        logSale = true;
        bodySupplierDelay = 1000;
        motorSupplierDelay = 1000;
        accessorySupplierDelay = 1000;
        dealerDelay = 2000;

        try (InputStream input = new FileInputStream(fileName)) {
            Properties prop = new Properties();
            prop.load(input);
            storageBodySize = Integer.parseInt(prop.getProperty("StorageBodySize", "100"));
            storageMotorSize = Integer.parseInt(prop.getProperty("StorageMotorSize", "100"));
            storageAccessorySize = Integer.parseInt(prop.getProperty("StorageAccessorySize", "100"));
            storageAutoSize = Integer.parseInt(prop.getProperty("StorageAutoSize", "100"));
            accessorySuppliers = Integer.parseInt(prop.getProperty("AccessorySuppliers", "5"));
            workers = Integer.parseInt(prop.getProperty("Workers", "10"));
            dealers = Integer.parseInt(prop.getProperty("Dealers", "20"));
            logSale = Boolean.parseBoolean(prop.getProperty("LogSale", "true"));

            bodySupplierDelay = Long.parseLong(prop.getProperty("BodySupplierDelay", "1000"));
            motorSupplierDelay = Long.parseLong(prop.getProperty("MotorSupplierDelay", "1000"));
            accessorySupplierDelay = Long.parseLong(prop.getProperty("AccessorySupplierDelay", "1000"));
            dealerDelay = Long.parseLong(prop.getProperty("DealerDelay", "2000"));
        } catch (Exception e) {
            System.out.println("Не удалось прочитать конфигурационный файл, используются значения по умолчанию. " + e.getMessage());
        }
    }
}
