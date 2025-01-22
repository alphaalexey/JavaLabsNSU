package ru.alspace;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class FrequencyAnalyzer {
    private final HashMap<String, Long> frequency = new HashMap<>();
    private long wordsCount = 0;

    public FrequencyAnalyzer(InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        reader.lines().forEach(this::parseLine);
    }

    private void parseLine(String line) {
        StringBuilder builder = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                builder.append(Character.toLowerCase(c));
            } else if (!builder.isEmpty()) {
                wordsCount++;
                final String word = builder.toString();
                if (!frequency.containsKey(word)) {
                    frequency.put(word, 1L);
                } else {
                    frequency.put(word, frequency.get(word) + 1);
                }
                builder = new StringBuilder();
            }
        }
        if (!builder.isEmpty()) {
            wordsCount++;
            final String word = builder.toString();
            if (!frequency.containsKey(word)) {
                frequency.put(word, 1L);
            } else {
                frequency.put(word, frequency.get(word) + 1);
            }
        }
    }

    public Map<String, Long> getFrequency() {
        return frequency;
    }

    public long getWordsCount() {
        return wordsCount;
    }
}
