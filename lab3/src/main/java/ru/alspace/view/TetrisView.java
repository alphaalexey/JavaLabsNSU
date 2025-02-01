package ru.alspace.view;

import ru.alspace.model.TetrisModel;
import ru.alspace.model.TetrisPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TetrisView extends JFrame {
    private final TetrisModel model;
    private final GamePanel gamePanel;

    // Элементы меню
    private final JMenuItem newGameItem;
    private final JMenuItem highScoresItem;
    private final JMenuItem aboutItem;
    private final JMenuItem exitItem;

    public TetrisView(TetrisModel model) {
        this.model = model;
        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Инициализация панели игры
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // Настройка меню
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        newGameItem = new JMenuItem("New Game");
        highScoresItem = new JMenuItem("High Scores");
        aboutItem = new JMenuItem("About");
        exitItem = new JMenuItem("Exit");

        gameMenu.add(newGameItem);
        gameMenu.add(highScoresItem);
        gameMenu.add(aboutItem);
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // Таймер для периодического перерисовывания панели игры
        Timer repaintTimer = new Timer(50, e -> gamePanel.repaint());
        repaintTimer.start();
    }

    // Методы для установки слушателей действий (ActionListener) для элементов меню
    public void addNewGameListener(ActionListener listener) {
        newGameItem.addActionListener(listener);
    }

    public void addHighScoresListener(ActionListener listener) {
        highScoresItem.addActionListener(listener);
    }

    public void addAboutListener(ActionListener listener) {
        aboutItem.addActionListener(listener);
    }

    public void addExitListener(ActionListener listener) {
        exitItem.addActionListener(listener);
    }

    public void showHighScoresDialog(String scoresText) {
        JOptionPane.showMessageDialog(this, scoresText, "High Scores", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Tetris Game\nAuthor: alphaalexey\nVersion: 1.0", "About", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showGameOverDialog(int score) {
        JOptionPane.showMessageDialog(this, "Game Over!\nYour score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    // Внутренний класс панели игры, отвечающий за отрисовку игрового поля и фигур.
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Заливка фона
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Вычисляем размеры ячейки
            int cellWidth = getWidth() / TetrisModel.BOARD_WIDTH;
            int cellHeight = getHeight() / TetrisModel.BOARD_HEIGHT;

            // Отрисовка уже зафиксированных фигур (игрового поля)
            int[][] board = model.getBoard();
            for (int i = 0; i < TetrisModel.BOARD_HEIGHT; i++) {
                for (int j = 0; j < TetrisModel.BOARD_WIDTH; j++) {
                    if (board[i][j] != 0) {
                        g.setColor(getColorForPiece(board[i][j]));
                        g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    }
                }
            }

            // Отрисовка текущей падающей фигуры
            TetrisPiece piece = model.getCurrentPiece();
            if (piece != null) {
                int[][] shape = piece.getShape();
                int pieceX = model.getCurrentX();
                int pieceY = model.getCurrentY();
                for (int i = 0; i < shape.length; i++) {
                    for (int j = 0; j < shape[i].length; j++) {
                        if (shape[i][j] != 0) {
                            int x = (pieceX + j) * cellWidth;
                            int y = (pieceY + i) * cellHeight;
                            g.setColor(getColorForPiece(shape[i][j]));
                            g.fillRect(x, y, cellWidth, cellHeight);
                            g.setColor(Color.DARK_GRAY);
                            g.drawRect(x, y, cellWidth, cellHeight);
                        }
                    }
                }
            }
        }

        // Метод сопоставления идентификатора фигуры с цветом.
        private Color getColorForPiece(int id) {
            return switch (id) {
                case 1 -> Color.CYAN;    // I
                case 2 -> Color.BLUE;    // J
                case 3 -> Color.ORANGE;  // L
                case 4 -> Color.YELLOW;  // O
                case 5 -> Color.GREEN;   // S
                case 6 -> Color.MAGENTA; // T
                case 7 -> Color.RED;     // Z
                default -> Color.WHITE;
            };
        }
    }
}
