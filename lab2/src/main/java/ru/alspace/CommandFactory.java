package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Фабрика команд. Загрузку классов осуществляет по имени команды,
 * используя конфигурацию из commands.properties.
 */
public class CommandFactory {
    private static final Logger logger = LogManager.getLogger(CommandFactory.class);
    private static final String DEFAULT_PROPERTIES_FILE = "/commands.properties";

    private final Properties config;

    public CommandFactory() {
        config = new Properties();
        loadDefaultConfig();
    }

    public CommandFactory(String configFile) {
        config = new Properties();
        loadConfig(configFile);
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
     * Создаёт объект команды по её имени (например, "PUSH").
     *
     * @param cmdName название команды
     * @return объект, реализующий интерфейс Command
     */
    public Command createCommand(String cmdName) {
        String className = config.getProperty(cmdName);
        if (className == null) {
            throw new RuntimeException("Команда '" + cmdName + "' не найдена в файле конфигурации.");
        }

        try {
            // Загружаем класс по строке
            Class<?> clazz = Class.forName(className);
            // Создаём экземпляр (должен реализовывать Command)
            Object instance = clazz.getDeclaredConstructor().newInstance();

            if (!(instance instanceof Command)) {
                throw new RuntimeException("Класс " + className + " не реализует интерфейс Command");
            }

            return (Command) instance;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании команды '" + cmdName +
                    "' (класс " + className + "): " + e.getMessage(), e);
        }
    }
}
