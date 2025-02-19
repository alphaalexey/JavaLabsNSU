package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Фабрика команд. Загружает конфигурацию из commands.properties,
 * инициализирует все команды и сохраняет их в Map, где ключ – имя команды,
 * а значение – объект, реализующий абстрактный класс Command.
 */
public class CommandFactory {
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);
    private static final String DEFAULT_PROPERTIES_FILE = "/commands.properties";

    private final Properties config;
    private final Map<String, Command> commandMap;

    public CommandFactory() {
        config = new Properties();
        commandMap = new HashMap<>();
        loadDefaultConfig();
        initializeCommands();
    }

    public CommandFactory(String configFile) {
        config = new Properties();
        commandMap = new HashMap<>();
        loadConfig(configFile);
        initializeCommands();
    }

    /**
     * Загружаем файл commands.properties (как ресурс).
     */
    private void loadDefaultConfig() {
        try (InputStream is = getClass().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (is == null) {
                throw new RuntimeException("Не найден стандартный конфиг");
            }
            config.load(is);
            logger.info("Стандартный конфиг загружен. Количество команд: {}", config.size());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке конфига: " + e.getMessage(), e);
        }
    }

    /**
     * Загружаем файл конфига.
     */
    private void loadConfig(String configFile) {
        try (InputStream is = new FileInputStream(configFile)) {
            config.load(is);
            logger.info("Файл конфига загружен. Количество команд: {}", config.size());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке конфига: " + e.getMessage(), e);
        }
    }

    /**
     * Инициализирует все команды, перечисленные в конфиге, и сохраняет их в Map.
     */
    private void initializeCommands() {
        for (String cmdName : config.stringPropertyNames()) {
            Command command = createCommand(cmdName);
            commandMap.put(cmdName, command);
            logger.info("Команда '{}' инициализирована.", cmdName);
        }
    }

    /**
     * Создаёт объект команды по её имени (например, "PUSH").
     *
     * @param cmdName название команды
     * @return объект, реализующий абстрактный класс Command
     */
    private Command createCommand(String cmdName) {
        String className = config.getProperty(cmdName);
        if (className == null) {
            throw new RuntimeException("Команда '" + cmdName + "' не найдена в файле конфигурации.");
        }

        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof Command)) {
                throw new RuntimeException("Класс " + className + " не реализует абстрактный класс Command");
            }

            return (Command) instance;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании команды '" + cmdName +
                    "' (класс " + className + "): " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает объект команды по её имени.
     *
     * @param cmdName название команды
     * @return объект, реализующий абстрактный класс Command
     */
    public Command getCommand(String cmdName) {
        Command command = commandMap.get(cmdName);
        if (command == null) {
            throw new RuntimeException("Команда '" + cmdName + "' не инициализирована.");
        }
        return command;
    }
}
