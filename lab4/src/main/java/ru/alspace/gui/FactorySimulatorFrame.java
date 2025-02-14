package ru.alspace.gui;

import ru.alspace.Config;
import ru.alspace.Dealer;
import ru.alspace.FactoryLogger;
import ru.alspace.controller.ProductionController;
import ru.alspace.model.Accessory;
import ru.alspace.model.Body;
import ru.alspace.model.Car;
import ru.alspace.model.Motor;
import ru.alspace.storage.Storage;
import ru.alspace.supplier.AccessorySupplier;
import ru.alspace.supplier.BodySupplier;
import ru.alspace.supplier.MotorSupplier;
import ru.alspace.threadpool.ThreadPool;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class FactorySimulatorFrame extends JFrame {
    private final Config config;

    // Склады
    private Storage<Body> bodyStorage;
    private Storage<Motor> motorStorage;
    private Storage<Accessory> accessoryStorage;
    private Storage<Car> carStorage;

    // Счётчики
    private final AtomicInteger bodyProduced = new AtomicInteger(0);
    private final AtomicInteger motorProduced = new AtomicInteger(0);
    private final AtomicInteger accessoryProduced = new AtomicInteger(0);
    private final AtomicInteger totalCarsSold = new AtomicInteger(0);

    // Поставщики
    private BodySupplier bodySupplier;
    private MotorSupplier motorSupplier;
    private AccessorySupplier accessorySupplier;

    // Дилеры
    private final ArrayList<Dealer> dealers = new ArrayList<>();

    // Пул сборщиков
    private ThreadPool threadPool;

    // Контроллер производства
    private ProductionController productionController;

    // UI-компоненты
    private JLabel lblBodyStorage;
    private JLabel lblMotorStorage;
    private JLabel lblAccessoryStorage;
    private JLabel lblCarStorage;
    private JLabel lblTaskQueue;
    private JLabel lblBodyProduced;
    private JLabel lblMotorProduced;
    private JLabel lblAccessoryProduced;
    private JLabel lblTotalCarsSold;

    private JSlider sliderBodySupplier;
    private JSlider sliderMotorSupplier;
    private JSlider sliderAccessorySupplier;
    private JSlider sliderDealer;

    private Timer uiTimer;

    public FactorySimulatorFrame(Config config) {
        this.config = config;
        initStorages();
        initThreads();
        initUI();
        startSimulation();
    }

    private void initStorages() {
        bodyStorage = new Storage<>(config.storageBodySize);
        motorStorage = new Storage<>(config.storageMotorSize);
        accessoryStorage = new Storage<>(config.storageAccessorySize);
        carStorage = new Storage<>(config.storageAutoSize);
    }

    private void initThreads() {
        // Инициализация поставщиков
        bodySupplier = new BodySupplier(bodyStorage, config.bodySupplierDelay, bodyProduced);
        motorSupplier = new MotorSupplier(motorStorage, config.motorSupplierDelay, motorProduced);
        accessorySupplier = new AccessorySupplier(accessoryStorage, config.accessorySupplierDelay, accessoryProduced);

        // Пул сборщиков
        threadPool = new ThreadPool(config.workers, 20);

        // Контроллер производства
        productionController = new ProductionController(threadPool, bodyStorage, motorStorage, accessoryStorage, carStorage);

        // Логгер продаж
        FactoryLogger factoryLogger = new FactoryLogger(config.logSale);

        // Инициализация дилеров
        for (int i = 0; i < config.dealers; i++) {
            Dealer dealer = new Dealer(i + 1, carStorage, config.dealerDelay, factoryLogger, totalCarsSold, productionController);
            dealers.add(dealer);
        }
    }

    private void initUI() {
        setTitle("Car Factory Simulator");
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Главная панель с отступами
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель управления (ползунки)
        JPanel controlPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        controlPanel.setBorder(new TitledBorder("Панель управления"));

        sliderBodySupplier = new JSlider(100, 2000, (int) config.bodySupplierDelay);
        sliderMotorSupplier = new JSlider(100, 2000, (int) config.motorSupplierDelay);
        sliderAccessorySupplier = new JSlider(100, 2000, (int) config.accessorySupplierDelay);
        sliderDealer = new JSlider(100, 5000, (int) config.dealerDelay);

        controlPanel.add(new JLabel("Задержка Body (ms):", SwingConstants.RIGHT));
        controlPanel.add(sliderBodySupplier);
        controlPanel.add(new JLabel("Задержка Motor (ms):", SwingConstants.RIGHT));
        controlPanel.add(sliderMotorSupplier);
        controlPanel.add(new JLabel("Задержка Accessory (ms):", SwingConstants.RIGHT));
        controlPanel.add(sliderAccessorySupplier);
        controlPanel.add(new JLabel("Задержка Dealer (ms):", SwingConstants.RIGHT));
        controlPanel.add(sliderDealer);

        // Панель склада
        JPanel storagePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        storagePanel.setBorder(new TitledBorder("Склады"));
        lblBodyStorage = new JLabel();
        lblMotorStorage = new JLabel();
        lblAccessoryStorage = new JLabel();
        lblCarStorage = new JLabel();
        storagePanel.add(lblBodyStorage);
        storagePanel.add(lblMotorStorage);
        storagePanel.add(lblAccessoryStorage);
        storagePanel.add(lblCarStorage);

        // Панель статистики производства
        JPanel statsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        statsPanel.setBorder(new TitledBorder("Статистика производства"));
        lblBodyProduced = new JLabel();
        lblMotorProduced = new JLabel();
        lblAccessoryProduced = new JLabel();
        lblTotalCarsSold = new JLabel();
        lblTaskQueue = new JLabel();
        statsPanel.add(new JLabel("Bodies Produced:", SwingConstants.RIGHT));
        statsPanel.add(lblBodyProduced);
        statsPanel.add(new JLabel("Motors Produced:", SwingConstants.RIGHT));
        statsPanel.add(lblMotorProduced);
        statsPanel.add(new JLabel("Accessories Produced:", SwingConstants.RIGHT));
        statsPanel.add(lblAccessoryProduced);

        // Панель продаж и задач ThreadPool
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        bottomPanel.setBorder(new TitledBorder("Продажи и сборка"));
        JPanel salesPanel = new JPanel(new BorderLayout());
        salesPanel.add(new JLabel("Total Cars Sold:", SwingConstants.CENTER), BorderLayout.NORTH);
        salesPanel.add(lblTotalCarsSold, BorderLayout.CENTER);
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.add(new JLabel("ThreadPool Queue:", SwingConstants.CENTER), BorderLayout.NORTH);
        queuePanel.add(lblTaskQueue, BorderLayout.CENTER);
        bottomPanel.add(salesPanel);
        bottomPanel.add(queuePanel);

        // Собираем центральную панель
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(storagePanel, BorderLayout.WEST);
        centerPanel.add(statsPanel, BorderLayout.CENTER);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Добавляем панели в mainPanel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);

        // Слушатели для ползунков (обновляем задержки, когда пользователь перестаёт двигать ползунок)
        sliderBodySupplier.addChangeListener((ChangeEvent e) -> {
            if (!sliderBodySupplier.getValueIsAdjusting()) {
                bodySupplier.delay = sliderBodySupplier.getValue();
            }
        });
        sliderMotorSupplier.addChangeListener((ChangeEvent e) -> {
            if (!sliderMotorSupplier.getValueIsAdjusting()) {
                motorSupplier.delay = sliderMotorSupplier.getValue();
            }
        });
        sliderAccessorySupplier.addChangeListener((ChangeEvent e) -> {
            if (!sliderAccessorySupplier.getValueIsAdjusting()) {
                accessorySupplier.delay = sliderAccessorySupplier.getValue();
            }
        });
        sliderDealer.addChangeListener((ChangeEvent e) -> {
            if (!sliderDealer.getValueIsAdjusting()) {
                int delay = sliderDealer.getValue();
                for (Dealer dealer : dealers) {
                    dealer.delay = delay;
                }
            }
        });

        // Таймер обновления статистики
        uiTimer = new Timer(500, e -> updateStats());
        uiTimer.start();
    }

    private void updateStats() {
        lblBodyStorage.setText("Body Storage: " + bodyStorage.size() + " / " + bodyStorage.getCapacity());
        lblMotorStorage.setText("Motor Storage: " + motorStorage.size() + " / " + motorStorage.getCapacity());
        lblAccessoryStorage.setText("Accessory Storage: " + accessoryStorage.size() + " / " + accessoryStorage.getCapacity());
        lblCarStorage.setText("Finished Cars: " + carStorage.size() + " / " + carStorage.getCapacity());
        lblTaskQueue.setText(String.valueOf(threadPool.getQueueSize()));
        lblBodyProduced.setText(String.valueOf(bodyProduced.get()));
        lblMotorProduced.setText(String.valueOf(motorProduced.get()));
        lblAccessoryProduced.setText(String.valueOf(accessoryProduced.get()));
        lblTotalCarsSold.setText(String.valueOf(totalCarsSold.get()));
    }

    private void startSimulation() {
        bodySupplier.start();
        motorSupplier.start();
        accessorySupplier.start();
        productionController.start();
        for (Dealer dealer : dealers) {
            dealer.start();
        }
    }

    // Метод корректного завершения работы (вызывается при закрытии окна)
    public void shutdown() {
        uiTimer.stop();
        bodySupplier.shutdown();
        motorSupplier.shutdown();
        accessorySupplier.shutdown();
        for (Dealer dealer : dealers) {
            dealer.shutdown();
        }
        productionController.shutdown();
        threadPool.shutdown();
        // Log4j завершится вместе с завершением приложения
    }
}
