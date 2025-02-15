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

    private boolean paused = false;

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
                // Если игра на паузе, игнорируем все клавиши, кроме P для переключения паузы
                if (paused && e.getKeyCode() != KeyEvent.VK_P) {
                    logger.debug("Key press ignored because game is paused");
                    return;
                }
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
                    case KeyEvent.VK_P:
                        // Переключение паузы по клавише P
                        if (!paused) {
                            pauseGame();
                        } else {
                            resumeGame();
                        }
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
            if (gameTimer == null || !gameTimer.isRunning()) {
                startGameLoop();
            }
            // При старте новой игры снимаем паузу
            paused = false;
        });
        view.addPauseListener(e -> {
            if (!paused) {
                pauseGame();
            } else {
                resumeGame();
            }
        });
        view.addHighScoresListener(e -> {
            String scores = highScoreManager.getHighScores();
            view.showHighScoresDialog(scores);
            logger.info("High Scores menu item selected");
        });
        view.addAboutListener(e -> {
            // Если игра не была на паузе до открытия диалога, ставим её на паузу
            boolean wasPaused = paused;
            if (!paused) {
                pauseGame();
            }
            view.showAboutDialog();
            // После закрытия About-диалога возобновляем игру, если она не была поставлена на паузу вручную
            if (!wasPaused) {
                resumeGame();
            }
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
            if (!model.isGameOver() && !paused) {
                model.moveDown();
            } else if (model.isGameOver()) {
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

    private void pauseGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            paused = true;
            view.setPaused(true);
            logger.info("Game paused");
        }
    }

    private void resumeGame() {
        if (gameTimer != null && !gameTimer.isRunning() && !model.isGameOver()) {
            gameTimer.start();
            paused = false;
            view.setPaused(false);
            logger.info("Game resumed");
        }
    }
}
