package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.model.ScoreRecord;
import ru.alspace.model.TetrisDifficulty;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HighScoreManager {
    private static final Logger logger = LogManager.getLogger(HighScoreManager.class);
    private static final String HIGH_SCORE_FILE = "highscores.txt";
    private static final int MAX_SCORES = 10;
    private List<ScoreRecord> scores;

    public HighScoreManager() {
        scores = new ArrayList<>();
        loadHighScores();
    }

    public void addScore(ScoreRecord score) {
        scores.add(score);
        scores.sort((r1, r2) -> Integer.compare(r2.score(), r1.score()));
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        logger.debug("Added score: {}. Scores list: {}", score, scores);
    }

    public String getHighScores() {
        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (ScoreRecord score : scores) {
            sb.append(rank++).append(". ").append(score).append("\n");
        }
        return sb.toString();
    }

    public void loadHighScores() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists()) {
            logger.info("High score file not found. Starting with empty high score list.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            scores.clear();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                try {
                    int score = Integer.parseInt(parts[0].trim());
                    // Сохраняем имя константы
                    TetrisDifficulty difficulty = Enum.valueOf(TetrisDifficulty.class, parts[1].trim().toUpperCase());
                    long gameTime = Long.parseLong(parts[2].trim());
                    scores.add(new ScoreRecord(score, difficulty, gameTime));
                } catch (IllegalArgumentException ex) {
                    logger.warn("Invalid score entry in high score file: {}", line);
                }
            }
            scores.sort((r1, r2) -> Integer.compare(r2.score(), r1.score()));
            logger.info("Loaded high scores: {}", scores);
        } catch (IOException e) {
            logger.error("Error loading high scores", e);
        }
    }

    public void saveHighScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (ScoreRecord score : scores) {
                writer.println(score.score() + "," + score.difficulty().name() + "," + score.gameTimeInMillis());
            }
            logger.info("Saved high scores: {}", scores);
        } catch (IOException e) {
            logger.error("Error saving high scores", e);
        }
    }
}
