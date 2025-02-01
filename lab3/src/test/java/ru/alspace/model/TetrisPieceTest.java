package ru.alspace.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TetrisPieceTest {
    @Test
    void testRotate() {
        int[][] shape = {
                {1, 0},
                {1, 1}
        };
        TetrisPiece piece = new TetrisPiece(shape);
        TetrisPiece rotated = piece.rotate();
        int[][] expected = {
                {1, 1},
                {1, 0}
        };
        int[][] rotatedShape = rotated.shape();
        assertEquals(expected.length, rotatedShape.length, "Rotated shape row count mismatch");
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], rotatedShape[i], "Row " + i + " mismatch");
        }
    }
}
