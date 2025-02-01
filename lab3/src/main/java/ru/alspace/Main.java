package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.controller.TetrisController;
import ru.alspace.model.TetrisModel;
import ru.alspace.view.TetrisView;

import javax.swing.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Tetris application");
        // Все операции с UI выполняем в потоке диспетчеризации событий (EDT)
        SwingUtilities.invokeLater(() -> {
            TetrisModel model = new TetrisModel();
            TetrisView view = new TetrisView(model);
            new TetrisController(model, view);
            view.setVisible(true);
            logger.info("Tetris UI is visible");
        });
    }
}
