package ru.alspace.model;

import java.util.Random;

public record TetrisPiece(int[][] shape) {
    public static final int MAX_SIZE = 6;

    public TetrisPiece {
        if (shape.length > MAX_SIZE) {
            throw new IllegalArgumentException("shape is too large");
        }
    }

    // Возвращает случайную фигуру из стандартного набора (I, J, L, O, S, T, Z)
    public static TetrisPiece getRandomPiece(Random random) {
        int r = random.nextInt(7);
        return switch (r) {
            case 0 -> new TetrisPiece(new int[][]{
                    {0, 0, 0, 0},
                    {1, 1, 1, 1},
                    {0, 0, 0, 0},
                    {0, 0, 0, 0}
            }); // I
            case 1 -> new TetrisPiece(new int[][]{
                    {2, 0, 0},
                    {2, 2, 2},
                    {0, 0, 0}
            }); // J
            case 2 -> new TetrisPiece(new int[][]{
                    {0, 0, 3},
                    {3, 3, 3},
                    {0, 0, 0}
            }); // L (идентификатор 3)
            case 3 -> new TetrisPiece(new int[][]{
                    {4, 4},
                    {4, 4}
            }); // O
            case 4 -> new TetrisPiece(new int[][]{
                    {0, 5, 5},
                    {5, 5, 0},
                    {0, 0, 0}
            }); // S
            case 5 -> new TetrisPiece(new int[][]{
                    {0, 6, 0},
                    {6, 6, 6},
                    {0, 0, 0}
            }); // T
            case 6 -> new TetrisPiece(new int[][]{
                    {7, 7, 0},
                    {0, 7, 7},
                    {0, 0, 0}
            }); // Z
            default -> null;
        };
    }

    // Метод поворота фигуры по часовой стрелке. Возвращается новый объект TetrisPiece.
    public TetrisPiece rotate() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                rotated[j][rows - 1 - i] = shape[i][j];
            }
        }
        return new TetrisPiece(rotated);
    }

    public int getYOffset() {
        int rows = shape.length;
        int cols = shape[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (shape[i][j] != 0) {
                    return i;
                }
            }
        }
        return 0; // Пустая фигура
    }
}
