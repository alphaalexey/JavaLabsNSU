package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    private static final Logger logger = LogManager.getLogger(HighScoreManager.class);

    private List<Integer> scores;
    private static final String HIGH_SCORE_FILE = "highscores.txt";
    private static final int MAX_SCORES = 10;

    public HighScoreManager() {
        scores = new ArrayList<>();
        loadHighScores();
    }

    public void addScore(int score) {
        scores.add(score);
        scores.sort(Collections.reverseOrder());
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        logger.debug("Added score: {}. Scores list: {}", score, scores);
    }

    public String getHighScores() {
        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (int score : scores) {
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
                try {
                    int score = Integer.parseInt(line.trim());
                    scores.add(score);
                } catch (NumberFormatException ex) {
                    logger.warn("Invalid score entry in high score file: {}", line);
                }
            }
            scores.sort(Collections.reverseOrder());
            logger.info("Loaded high scores: {}", scores);
        } catch (IOException e) {
            logger.error("Error loading high scores", e);
        }
    }

    public void saveHighScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HIGH_SCORE_FILE))) {
            for (int score : scores) {
                writer.println(score);
            }
            logger.info("Saved high scores: {}", scores);
        } catch (IOException e) {
            logger.error("Error saving high scores", e);
        }
    }
}
