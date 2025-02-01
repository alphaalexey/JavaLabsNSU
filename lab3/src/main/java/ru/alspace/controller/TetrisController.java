package ru.alspace.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.HighScoreManager;
import ru.alspace.model.TetrisModel;
import ru.alspace.view.TetrisView;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TetrisController {
    private static final Logger logger = LogManager.getLogger(TetrisController.class);

    private final TetrisModel model;
    private final TetrisView view;
    private Timer gameTimer;
    private final HighScoreManager highScoreManager;

    public TetrisController(TetrisModel model, TetrisView view) {
        this.model = model;
        this.view = view;
        highScoreManager = new HighScoreManager();
        initController();
        startGameLoop();
    }

    private void initController() {
        // Обработка нажатий клавиш (все действия производятся в UI потоке)
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (model.isGameOver()) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        model.moveLeft();
                        logger.debug("Left arrow pressed");
                        break;
                    case KeyEvent.VK_RIGHT:
                        model.moveRight();
                        logger.debug("Right arrow pressed");
                        break;
                    case KeyEvent.VK_UP:
                        model.rotate();
                        logger.debug("Up arrow pressed for rotation");
                        break;
                    case KeyEvent.VK_DOWN:
                        model.moveDown();
                        logger.debug("Down arrow pressed");
                        break;
                }
            }
        });
        // Чтобы окно получало фокус для обработки нажатий клавиш
        view.setFocusable(true);
        view.requestFocusInWindow();

        // Обработка команд из меню
        view.addNewGameListener(e -> {
            logger.info("New Game menu item selected");
            model.newGame();
            view.requestFocusInWindow();
            // Запускаем игровой цикл, если игра была завершена
            if (!gameTimer.isRunning()) {
                startGameLoop();
            }
        });
        view.addHighScoresListener(e -> {
            String scores = highScoreManager.getHighScores();
            view.showHighScoresDialog(scores);
            logger.info("High Scores menu item selected");
        });
        view.addAboutListener(e -> {
            view.showAboutDialog();
            logger.info("About menu item selected");
        });
        view.addExitListener(e -> {
            logger.info("Exit menu item selected. Exiting application.");
            System.exit(0);
        });
    }

    private void startGameLoop() {
        // Таймер игрового цикла – каждые 500 мс фигура опускается на одну строку.
        gameTimer = new Timer(500, e -> {
            if (!model.isGameOver()) {
                model.moveDown();
            } else {
                gameTimer.stop();
                // Сохраняем результат в таблицу рекордов
                highScoreManager.addScore(model.getScore());
                highScoreManager.saveHighScores();
                view.showGameOverDialog(model.getScore());
                logger.info("Game over. Final score: {}", model.getScore());
            }
            view.requestFocusInWindow();
        });
        gameTimer.start();
        logger.info("Game loop started");
    }
}
