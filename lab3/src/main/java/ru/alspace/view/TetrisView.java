package ru.alspace.view;

import ru.alspace.model.TetrisDifficulty;
import ru.alspace.model.TetrisModel;
import ru.alspace.model.TetrisPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TetrisView extends JFrame {
    private final TetrisModel model;
    private final GamePanel gamePanel;

    // Компоненты для отображения текущего счёта и следующей фигуры
    private final JLabel scoreLabel;
    private final NextPiecePanel nextPiecePanel;
    private final JLabel piecesCountLabel;
    private final JLabel difficultyLabel;

    // Элементы меню
    private final JMenuItem newGameItem;
    private final JMenuItem difficultyItem;
    private final JMenuItem pauseItem;
    private final JMenuItem highScoresItem;
    private final JMenuItem aboutItem;
    private final JMenuItem exitItem;

    private boolean paused = false;

    public TetrisView(TetrisModel model) {
        this.model = model;
        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Инициализация панели игры
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // Инициализация информационной панели
        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(200, 600));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        scoreLabel = new JLabel("Score: 0");
        JLabel nextPieceLabel = new JLabel("Next:");
        nextPiecePanel = new NextPiecePanel();
        piecesCountLabel = new JLabel("Pieces count: 0");
        difficultyLabel = new JLabel("Difficulty: " + TetrisDifficulty.getDefault().getName());
        nextPiecePanel.setPreferredSize(new Dimension(80, 80));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextPieceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextPiecePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        piecesCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(scoreLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(nextPieceLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(nextPiecePanel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(piecesCountLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(difficultyLabel);
        add(infoPanel, BorderLayout.EAST);

        // Настройка меню
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        newGameItem = new JMenuItem("New Game");
        difficultyItem = new JMenuItem("Difficulty");
        pauseItem = new JMenuItem("Pause/Resume");
        highScoresItem = new JMenuItem("High Scores");
        aboutItem = new JMenuItem("About");
        exitItem = new JMenuItem("Exit");

        gameMenu.add(newGameItem);
        gameMenu.add(difficultyItem);
        gameMenu.add(pauseItem);
        gameMenu.add(highScoresItem);
        gameMenu.add(aboutItem);
        gameMenu.add(exitItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        gamePanel.repaint();
    }

    public void setDifficulty(TetrisDifficulty difficulty) {
        difficultyLabel.setText("Difficulty: " + difficulty.getName());
    }

    public void updatePanel() {
        gamePanel.repaint();
    }

    public void updateInfo() {
        // Обновление метки с текущим счётом
        scoreLabel.setText("Score: " + model.getScore());
        // Обновление метки со следующим типом фигуры
        nextPiecePanel.repaint();
        piecesCountLabel.setText("Pieces count: " + model.getPiecesPlaced());
    }

    // Методы для установки слушателей действий (ActionListener) для элементов меню
    public void addNewGameListener(ActionListener listener) {
        newGameItem.addActionListener(listener);
    }

    public void addDifficultyListener(ActionListener listener) {
        difficultyItem.addActionListener(listener);
    }

    public void addPauseListener(ActionListener listener) {
        pauseItem.addActionListener(listener);
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
        String message = """
                Tetris Game
                Author: alphaalexey
                Version: 1.0
                
                How to Play:
                 - Use LEFT and RIGHT arrow keys to move the piece
                 - Use UP arrow key to rotate the piece
                 - Use DOWN arrow key to speed up the drop
                 - Use SPACE arrow key for immediate drop
                 - Press 'P' to pause/resume the game""";
        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showGameOverDialog(int score) {
        JOptionPane.showMessageDialog(this, "Game Over!\nYour score: " + score, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

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

    // Внутренний класс панели игры, отвечающий за отрисовку игрового поля, фигур и фона-сетки.
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Заливка всего фона
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Определяем размер панели
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            // Вычисляем масштаб, чтобы сохранить пропорции игрового поля (BOARD_WIDTH x BOARD_HEIGHT)
            double scale = Math.min((double) panelWidth / TetrisModel.BOARD_WIDTH, (double) panelHeight / TetrisModel.BOARD_HEIGHT);
            int boardPixelWidth = (int) (TetrisModel.BOARD_WIDTH * scale);
            int boardPixelHeight = (int) (TetrisModel.BOARD_HEIGHT * scale);
            // Центрируем игровое поле
            int offsetX = (panelWidth - boardPixelWidth) / 2;
            int offsetY = (panelHeight - boardPixelHeight) / 2;

            // Отрисовка фон-сетки (вертикальные и горизонтальные линии)
            g.setColor(Color.DARK_GRAY);
            // Вертикальные линии
            for (int i = 0; i <= TetrisModel.BOARD_WIDTH; i++) {
                int x = offsetX + (int) (i * scale);
                g.drawLine(x, offsetY, x, offsetY + boardPixelHeight);
            }
            // Горизонтальные линии
            for (int i = 0; i <= TetrisModel.BOARD_HEIGHT; i++) {
                int y = offsetY + (int) (i * scale);
                g.drawLine(offsetX, y, offsetX + boardPixelWidth, y);
            }

            // Отрисовка уже зафиксированных фигур (игрового поля)
            int[][] board = model.getBoard();
            for (int i = 0; i < TetrisModel.BOARD_HEIGHT; i++) {
                for (int j = 0; j < TetrisModel.BOARD_WIDTH; j++) {
                    if (board[i][j] != 0) {
                        g.setColor(getColorForPiece(board[i][j]));
                        int cellX = offsetX + (int) (j * scale);
                        int cellY = offsetY + (int) (i * scale);
                        int cellSize = (int) scale;
                        g.fillRect(cellX, cellY, cellSize, cellSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(cellX, cellY, cellSize, cellSize);
                    }
                }
            }

            // Отрисовка ghost-фигуры (прозрачный образ того, куда упадёт фигура)
            if (model.getCurrentPiece() != null) {
                int ghostY = model.getGhostY();
                int[][] shape = model.getCurrentPiece().shape();
                Graphics2D g2d = (Graphics2D) g;
                Composite originalComposite = g2d.getComposite();
                // Используем 30%-ую непрозрачность для ghost-фигуры
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                for (int i = 0; i < shape.length; i++) {
                    for (int j = 0; j < shape[i].length; j++) {
                        if (shape[i][j] != 0) {
                            int x = offsetX + (int) ((model.getCurrentX() + j) * scale);
                            int y = offsetY + (int) ((ghostY + i) * scale);
                            int cellSize = (int) scale;
                            g2d.setColor(getColorForPiece(shape[i][j]));
                            g2d.fillRect(x, y, cellSize, cellSize);
                        }
                    }
                }
                g2d.setComposite(originalComposite);
            }

            // Отрисовка текущей падающей фигуры
            TetrisPiece piece = model.getCurrentPiece();
            if (piece != null) {
                int[][] shape = piece.shape();
                int pieceX = model.getCurrentX();
                int pieceY = model.getCurrentY();
                for (int i = 0; i < shape.length; i++) {
                    for (int j = 0; j < shape[i].length; j++) {
                        if (shape[i][j] != 0) {
                            g.setColor(getColorForPiece(shape[i][j]));
                            int x = offsetX + (int) ((pieceX + j) * scale);
                            int y = offsetY + (int) ((pieceY + i) * scale);
                            int cellSize = (int) scale;
                            g.fillRect(x, y, cellSize, cellSize);
                            g.setColor(Color.BLACK);
                            g.drawRect(x, y, cellSize, cellSize);
                        }
                    }
                }
            }

            if (paused) {
                g.setColor(new Color(0, 0, 0, 150)); // полупрозрачный черный фон
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.setFont(g.getFont().deriveFont(Font.BOLD, 36f));
                FontMetrics fm = g.getFontMetrics();
                String message = "Game Paused";
                int textWidth = fm.stringWidth(message);
                int textHeight = fm.getHeight();
                g.drawString(message, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2);
            }
        }
    }

    private class NextPiecePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            TetrisPiece next = model.getNextPiece();
            if (next == null) {
                return;
            }
            int[][] shape = next.shape();
            // Определяем размер ячейки, исходя из размера панели
            int rows = shape.length;
            int cols = shape[0].length;
            int size = TetrisPiece.MAX_SIZE;
            int cellSize = Math.min(getWidth() / size, getHeight() / size);
            // Центрируем фигуру в панели
            int totalWidth = cellSize * size;
            int totalHeight = cellSize * size;
            int offsetX = (getWidth() - totalWidth) / 2;
            int offsetY = (getHeight() - totalHeight) / 2;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (shape[i][j] != 0) {
                        g.setColor(getColorForPiece(shape[i][j]));
                        int x = offsetX + j * cellSize;
                        int y = offsetY + i * cellSize;
                        g.fillRect(x, y, cellSize, cellSize);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, cellSize, cellSize);
                    }
                }
            }
        }
    }
}
