package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.fatal("Incorrect number of arguments");

            System.out.println("Usage: java -jar lab1 INPUT OUTPUT");
            System.out.println("  INPUT - path of file to process");
            System.out.println("  OUTPUT - path to result csv file");
            return;
        }

        final String inputFile = args[0];
        final String outputFile = args[1];

        final Map<String, Long> frequencies;
        final long wordsCount;
        try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
            logger.info("Processing file `{}`", inputFile);

            FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer(fileInputStream);
            frequencies = frequencyAnalyzer.getFrequency();
            wordsCount = frequencyAnalyzer.getWordsCount();
        } catch (IOException e) {
            logger.error("Failed opening input file `{}`", inputFile, e);
            System.out.println("Cannot open input file");
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            logger.info("Writing result to file `{}`", outputFile);

            // Write header
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write("Слово, Частота, Частота (в %)");
            bufferedWriter.newLine();

            // Write lines
            for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
                long frequency = entry.getValue();
                long percentage = frequency * 100 / wordsCount;

                bufferedWriter.write(entry.getKey());
                bufferedWriter.write(", ");
                bufferedWriter.write(String.valueOf(frequency));
                bufferedWriter.write(", ");
                bufferedWriter.write(String.valueOf(percentage));
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("Failed opening output file `{}`", outputFile, e);
            System.out.println("Cannot open output file");
        }
    }
}
