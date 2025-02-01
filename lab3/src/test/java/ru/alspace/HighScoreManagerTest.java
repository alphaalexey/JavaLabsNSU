package ru.alspace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
        manager.addScore(100);
        manager.addScore(200);
        manager.addScore(150);
        String scoresText = manager.getHighScores();
        // Ожидаем, что самый высокий счёт окажется первым
        assertTrue(scoresText.contains("1. 200"), "High score order incorrect");
        assertTrue(scoresText.contains("2. 150"), "High score order incorrect");
        assertTrue(scoresText.contains("3. 100"), "High score order incorrect");
    }

    @Test
    void testSaveAndLoadHighScores() {
        HighScoreManager manager = new HighScoreManager();
        manager.addScore(300);
        manager.saveHighScores();

        // Создаём новый экземпляр, который должен загрузить сохранённые рекорды
        HighScoreManager manager2 = new HighScoreManager();
        String scoresText = manager2.getHighScores();
        assertTrue(scoresText.contains("1. 300"), "Failed to load saved high scores");
    }
}
