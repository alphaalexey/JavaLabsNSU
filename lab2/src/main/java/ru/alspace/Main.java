package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            BufferedReader reader;
            if (args.length > 0) {
                // Если есть аргумент – воспринимаем его как имя файла
                String filename = args[0];
                logger.info("Запуск калькулятора. Чтение команд из файла: {}", filename);
                reader = new BufferedReader(new FileReader(filename));
            } else {
                // Иначе читаем команды из консоли (стандартный ввод)
                logger.info("Запуск калькулятора. Чтение команд из стандартного потока ввода.");
                reader = new BufferedReader(new InputStreamReader(System.in));
            }

            // Создаём "движок" калькулятора
            CalculatorEngine engine = new CalculatorEngine();
            // Запускаем обработку команд
            engine.run(reader);

        } catch (Exception e) {
            logger.error("Ошибка запуска калькулятора: {}", e.getMessage(), e);
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
