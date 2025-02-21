package ru.alspace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.alspace.model.ScoreRecord;
import ru.alspace.model.TetrisDifficulty;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HighScoreManagerTest {
    private static final String HIGH_SCORE_FILE = "highscores.txt";

    @AfterEach
    void cleanup() {
        // Удаляем файл таблицы рекордов после каждого теста
        File file = new File(HIGH_SCORE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testAddAndRetrieveScores() {
        HighScoreManager manager = new HighScoreManager();
        // Добавляем несколько записей с разными очками, сложностями и временем игры
        manager.addScore(new ScoreRecord(100, TetrisDifficulty.NORMAL, 10));
        manager.addScore(new ScoreRecord(200, TetrisDifficulty.HARD, 15));
        manager.addScore(new ScoreRecord(150, TetrisDifficulty.EASY, 20));
        String scoresText = manager.getHighScores();
        // Проверяем, что самый высокий счёт (200) находится на первом месте, затем 150 и 100
        assertTrue(scoresText.contains("1. 200"), "High score order incorrect");
        assertTrue(scoresText.contains("2. 150"), "High score order incorrect");
        assertTrue(scoresText.contains("3. 100"), "High score order incorrect");
    }

    @Test
    void testSaveAndLoadHighScores() {
        HighScoreManager manager = new HighScoreManager();
        manager.addScore(new ScoreRecord(300, TetrisDifficulty.NORMAL, 25));
        manager.saveHighScores();

        // Создаём новый экземпляр, который должен загрузить сохранённые рекорды
        HighScoreManager manager2 = new HighScoreManager();
        String scoresText = manager2.getHighScores();
        assertTrue(scoresText.contains("1. 300"), "Failed to load saved high scores");
    }
}
