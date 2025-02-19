package ru.alspace.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class TetrisModel {
    private static final Logger logger = LogManager.getLogger(TetrisModel.class);

    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;

    private final int[][] board;
    private TetrisPiece currentPiece;
    private int currentX, currentY;
    private TetrisPiece nextPiece;
    private int score;
    private boolean gameOver;
    private int piecesPlaced;
    private final Random random;

    public TetrisModel() {
        board = new int[BOARD_HEIGHT][BOARD_WIDTH];
        random = new Random();
        newGame();
    }

    // Начало новой игры: очистка игрового поля, сброс счёта и установка первой фигуры
    public void newGame() {
        logger.info("Starting new game");
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = 0;
            }
        }
        score = 0;
        gameOver = false;
        piecesPlaced = 0;
        currentPiece = TetrisPiece.getRandomPiece(random);
        // Выравниваем стартовую позицию (учитываем, что фигура может иметь размерность 3x3 или 4x4)
        currentX = BOARD_WIDTH / 2 - 2;
        currentY = 0;
        nextPiece = TetrisPiece.getRandomPiece(random);
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getPiecesPlaced() {
        return piecesPlaced;
    }

    public int[][] getBoard() {
        return board;
    }

    public TetrisPiece getCurrentPiece() {
        return currentPiece;
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }

    public TetrisPiece getNextPiece() {
        return nextPiece;
    }

    // Пытаемся сдвинуть фигуру вниз; если двигаться нельзя – «закрепляем» фигуру и обрабатываем заполненные линии.
    public void moveDown() {
        if (!gameOver && canMove(currentPiece, currentX, currentY + 1)) {
            currentY++;
            logger.debug("Moved piece down to y={}", currentY);
        } else {
            logger.debug("Cannot move down; fixing current piece");
            fixCurrentPiece();
            clearLines();
            spawnNextPiece();
        }
    }

    public void moveLeft() {
        if (!gameOver && canMove(currentPiece, currentX - 1, currentY)) {
            currentX--;
            logger.debug("Moved piece left to x={}", currentX);
        }
    }

    public void moveRight() {
        if (!gameOver && canMove(currentPiece, currentX + 1, currentY)) {
            currentX++;
            logger.debug("Moved piece right to x={}", currentX);
        }
    }

    public void rotate() {
        if (gameOver) return;
        TetrisPiece rotated = currentPiece.rotate();
        if (canMove(rotated, currentX, currentY)) {
            currentPiece = rotated;
            logger.debug("Rotated piece");
        }
    }

    // Попытка заспавнить следующую фигуру. Если фигуру разместить сразу нельзя – игра окончена.
    private void spawnNextPiece() {
        piecesPlaced++;
        currentPiece = nextPiece;
        currentX = BOARD_WIDTH / 2 - 2;
        currentY = 0;
        nextPiece = TetrisPiece.getRandomPiece(random);
        if (!canMove(currentPiece, currentX, currentY)) {
            gameOver = true;
            logger.info("Game Over. Final score: {}", score);
        }
    }

    // Проверяем, можно ли разместить фигуру piece с позицией смещения (x,y)
    public boolean canMove(TetrisPiece piece, int x, int y) {
        int[][] shape = piece.shape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
                        return false;
                    }
                    if (board[newY][newX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // «Закрепляем» текущую фигуру на игровом поле (переносим значения в массив board)
    private void fixCurrentPiece() {
        int[][] shape = currentPiece.shape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardX = currentX + j;
                    int boardY = currentY + i;
                    if (boardY >= 0 && boardY < BOARD_HEIGHT && boardX >= 0 && boardX < BOARD_WIDTH) {
                        board[boardY][boardX] = shape[i][j];
                    }
                }
            }
        }
        logger.debug("Fixed current piece on board");
    }

    // Проверяем все линии; при обнаружении заполненной линии – удаляем её и увеличиваем счёт.
    private void clearLines() {
        int linesCleared = 0;
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            boolean fullLine = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                removeLine(i);
                linesCleared++;
                logger.debug("Cleared line at index {}", i);
            }
        }
        score += linesCleared * 100;
        if (linesCleared > 0) {
            logger.info("Cleared {} lines. New score: {}", linesCleared, score);
        }
    }

    // Удаляем строку и сдвигаем верхние строки вниз.
    private void removeLine(int line) {
        for (int i = line; i > 0; i--) {
            System.arraycopy(board[i - 1], 0, board[i], 0, BOARD_WIDTH);
        }
        for (int j = 0; j < BOARD_WIDTH; j++) {
            board[0][j] = 0;
        }
    }
}
