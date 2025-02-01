package ru.alspace.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TetrisModelTest {
    private TetrisModel model;

    @BeforeEach
    void setup() {
        model = new TetrisModel();
    }

    @Test
    void testNewGameResetsBoardAndScore() {
        model.newGame();
        int[][] board = model.getBoard();
        for (int i = 0; i < TetrisModel.BOARD_HEIGHT; i++) {
            for (int j = 0; j < TetrisModel.BOARD_WIDTH; j++) {
                assertEquals(0, board[i][j], "Board cell should be reset to 0");
            }
        }
        assertEquals(0, model.getScore(), "Score should be reset to 0");
    }

    @Test
    void testCanMoveWithinBoard() {
        // Создаём простую фигуру 1x1 для тестирования
        TetrisPiece piece = new TetrisPiece(new int[][]{{1}});
        // Проверяем, что фигуру можно разместить внутри доски
        assertTrue(model.canMove(piece, 0, 0));
        // Проверяем, что попытка разместить вне доски возвращает false
        assertFalse(model.canMove(piece, -1, 0));
        assertFalse(model.canMove(piece, 0, -1));
        assertFalse(model.canMove(piece, TetrisModel.BOARD_WIDTH, 0));
        assertFalse(model.canMove(piece, 0, TetrisModel.BOARD_HEIGHT));
    }
}
