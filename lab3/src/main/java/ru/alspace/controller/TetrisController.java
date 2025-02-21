package ru.alspace.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.HighScoreManager;
import ru.alspace.model.ScoreRecord;
import ru.alspace.model.TetrisDifficulty;
import ru.alspace.model.TetrisModel;
import ru.alspace.view.TetrisView;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TetrisController {
    private static final Logger logger = LogManager.getLogger(TetrisController.class);

    private TetrisDifficulty currentDifficulty;
    private TetrisDifficulty nextDifficulty;

    private final TetrisModel model;
    private final TetrisView view;
    private Timer gameTimer;
    private final HighScoreManager highScoreManager;

    private long gameStartTime;
    private int lastPiecesPlaced = 0;

    private boolean paused = false;

    public TetrisController(TetrisModel model, TetrisView view) {
        this.model = model;
        this.view = view;
        highScoreManager = new HighScoreManager();
        currentDifficulty = TetrisDifficulty.getDefault();
        nextDifficulty = TetrisDifficulty.getDefault();
        initController();
        gameStartTime = System.currentTimeMillis();
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
                    case KeyEvent.VK_SPACE:
                        model.dropPiece();
                        logger.debug("Hard drop triggered");
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
            resumeGame();
            model.newGame();
            gameStartTime = System.currentTimeMillis();
            currentDifficulty = nextDifficulty;
            view.setDifficulty(currentDifficulty);
            view.requestFocusInWindow();
            if (gameTimer == null || !gameTimer.isRunning()) {
                startGameLoop();
            }
            // При старте новой игры снимаем паузу
            paused = false;
        });
        view.addDifficultyListener(e -> {
            logger.info("Difficulty menu item selected");
            // Если игра не была на паузе до открытия диалога, ставим её на паузу
            boolean wasPaused = paused;
            if (!paused) {
                pauseGame();
            }
            showDifficultyDialog();
            // После закрытия Difficulty-диалога возобновляем игру, если она не была поставлена на паузу вручную
            if (!wasPaused) {
                resumeGame();
            }
        });
        view.addPauseListener(e -> {
            logger.info("Pause/Resume menu item selected");
            if (!paused) {
                pauseGame();
            } else {
                resumeGame();
            }
        });
        view.addHighScoresListener(e -> {
            logger.info("High Scores menu item selected");
            // Если игра не была на паузе до открытия диалога, ставим её на паузу
            boolean wasPaused = paused;
            if (!paused) {
                pauseGame();
            }
            String scores = highScoreManager.getHighScores();
            view.showHighScoresDialog(scores);
            // После закрытия HighScores-диалога возобновляем игру, если она не была поставлена на паузу вручную
            if (!wasPaused) {
                resumeGame();
            }
        });
        view.addAboutListener(e -> {
            logger.info("About menu item selected");
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
        });
        view.addExitListener(e -> {
            logger.info("Exit menu item selected. Exiting application.");
            System.exit(0);
        });
    }

    private void startGameLoop() {
        // Таймер игрового цикла – время, за которое фигура опускается на одну строку. Уменьшается со временем
        gameTimer = new Timer(currentDifficulty.getInitialDelay(), e -> {
            if (!model.isGameOver() && !paused) {
                model.moveDown();
                if (model.getPiecesPlaced() > lastPiecesPlaced) {
                    lastPiecesPlaced = model.getPiecesPlaced();
                    int newDelay = Math.max(currentDifficulty.getMinDelay(),
                            currentDifficulty.getInitialDelay() - lastPiecesPlaced * currentDifficulty.getAccelerationDelta());
                    gameTimer.setDelay(newDelay);
                    logger.info("Game accelerated: new delay = {}", newDelay);
                }
            } else if (model.isGameOver()) {
                gameTimer.stop();
                // Сохраняем результат в таблицу рекордов
                long gameEndTime = System.currentTimeMillis();
                long gameTimeMillis = gameEndTime - gameStartTime;
                logger.info("gameEndTime = {}, gameStartTime = {}, gameTimeMillis = {}", gameEndTime, gameStartTime, gameTimeMillis);
                highScoreManager.addScore(new ScoreRecord(model.getScore(), currentDifficulty, gameTimeMillis));
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

    private void showDifficultyDialog() {
        TetrisDifficulty[] difficulties = TetrisDifficulty.values();
        TetrisDifficulty selected = (TetrisDifficulty) JOptionPane.showInputDialog(
                view,
                "Select Difficulty (applied to the next game):",
                "Difficulty Settings",
                JOptionPane.PLAIN_MESSAGE,
                null,
                difficulties,
                nextDifficulty);
        if (selected != null) {
            nextDifficulty = selected;
            logger.info("Difficulty set to {}: initialDelay={}, accelerationDelta={}",
                    nextDifficulty.getName(),
                    nextDifficulty.getInitialDelay(),
                    nextDifficulty.getAccelerationDelta());
        }
    }
}
