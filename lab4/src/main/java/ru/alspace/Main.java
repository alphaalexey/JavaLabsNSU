package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.gui.FactorySimulatorFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Запуск приложения CarFactory Simulator...");

        // Чтение конфигурации из файла
        Config config = new Config("config.properties");

        SwingUtilities.invokeLater(() -> {
            FactorySimulatorFrame frame = new FactorySimulatorFrame(config);
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    logger.info("Получен сигнал завершения. Завершаем работу...");
                    frame.shutdown();
                }
            });
        });
    }
}
