package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Проверяем число аргументов
        if (args.length > 2) {
            System.err.println("Слишком много аргументов!");
            System.err.println("Использование:");
            System.err.println("    java -jar MyCalc.jar                (читать команды из stdin, стандартный конфиг)");
            System.err.println("    java -jar MyCalc.jar <scriptFile>   (читать команды из scriptFile, стандартный конфиг)");
            System.err.println("    java -jar MyCalc.jar <scriptFile> <configFile> (читать команды из scriptFile, конфиг из configFile)");
            System.exit(1);
        }

        try {
            BufferedReader reader;
            if (args.length > 0) {
                // Первый аргумент – воспринимаем как имя файла команд
                String filename = args[0];
                logger.info("Запуск калькулятора. Чтение команд из файла: {}", filename);
                reader = new BufferedReader(new FileReader(filename));
            } else {
                // Иначе читаем команды из консоли (стандартный ввод)
                logger.info("Запуск калькулятора. Чтение команд из стандартного потока ввода.");
                reader = new BufferedReader(new InputStreamReader(System.in));
            }

            // Если у нас есть второй аргумент, пробуем трактовать его как путь к конфигу
            CommandFactory factory;
            if (args.length == 2) {
                String configFile = args[1];
                logger.info("Используем кастомный конфиг: {}", configFile);
                factory = new CommandFactory(configFile);
            } else {
                logger.info("Используем стандартный конфиг (commands.properties из ресурсов)");
                factory = new CommandFactory(); // загрузит default
            }
            // Инициализируем контекст (стек + defines)
            Context context = new Context(new Stack<>(), new HashMap<>());

            // Читаем файл/поток построчно
            String line;
            while ((line = reader.readLine()) != null) {
                // Удалим пробелы, проверим пустую строку или комментарий
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Разбиваем на токены
                String[] tokens = line.split("\\s+");
                String cmdName = tokens[0]; // Имя команды
                List<String> cmdArgs = new ArrayList<>();
                if (tokens.length > 1) {
                    // Аргументы — всё, что после 0-го
                    cmdArgs = Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length));
                }

                try {
                    // Создаём команду через фабрику
                    Command command = factory.getCommand(cmdName.toUpperCase());
                    // Вызываем execute
                    command.execute(context, cmdArgs);
                } catch (CommandExecutionException e) {
                    // Ошибка исполнения самой команды (например, недостаточно элементов в стеке)
                    logger.error("Ошибка при выполнении команды '{}': {}", cmdName, e.getMessage());
                    System.err.println("Ошибка исполнения команды '" + cmdName + "': " + e.getMessage());
                    // Продолжаем цикл (не прерываем программу)
                } catch (RuntimeException e) {
                    // Ошибка фабрики, не найдена команда, ошибка загрузки класса и т.д.
                    logger.error("Ошибка фабрики или системная: {}", e.getMessage(), e);
                    System.err.println("Невозможно выполнить команду '" + cmdName + "': " + e.getMessage());
                    // Тоже не прерываем
                }
            }

            logger.info("Программа завершила обработку команд.");
        } catch (Exception e) {
            logger.error("Глобальная ошибка: {}", e.getMessage(), e);
            System.err.println("Фатальная ошибка: " + e.getMessage());
        }
    }
}
