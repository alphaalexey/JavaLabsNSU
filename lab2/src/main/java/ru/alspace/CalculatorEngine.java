package ru.alspace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CalculatorEngine {
    private static final Logger logger = LogManager.getLogger(CalculatorEngine.class);

    // Основные структуры калькулятора:
    private final Stack<Double> stack = new Stack<>();
    private final Map<String, Double> defines = new HashMap<>();

    /**
     * Запуск основного цикла чтения строк (команд) и их выполнение.
     */
    public void run(BufferedReader reader) {
        logger.info("Начало чтения и выполнения команд.");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (Exception e) {
            logger.error("Ошибка в процессе выполнения команд: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при выполнении команд: " + e.getMessage(), e);
        } finally {
            logger.info("Завершение работы калькулятора.");
        }
    }

    /**
     * Обработка одной строки (возможно, содержащей команду).
     */
    private void processLine(String line) {
        line = line.trim();
        // Пропускаем пустые строки
        if (line.isEmpty()) {
            return;
        }
        // Игнорируем строки-комментарии, начинающиеся с '#'
        if (line.startsWith("#")) {
            logger.debug("Комментарий (пропускаем): {}", line);
            return;
        }

        // Разбиваем на токены
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toUpperCase();

        logger.info("Выполнение команды: {}", line);

        // Выполним команду
        executeCommand(command, tokens);
    }

    /**
     * Выполняет команду, используя текущий стек и карту defines.
     */
    public void executeCommand(String command, String[] tokens) {
        switch (command) {
            case "DEFINE":
                cmdDefine(tokens);
                break;

            case "PUSH":
                cmdPush(tokens);
                break;

            case "POP":
                cmdPop();
                break;

            case "PRINT":
                cmdPrint();
                break;

            case "+":
                cmdPlus();
                break;

            case "-":
                cmdMinus();
                break;

            case "*":
                cmdMul();
                break;

            case "/":
                cmdDiv();
                break;

            case "SQRT":
                cmdSqrt();
                break;

            default:
                throw new RuntimeException("Неизвестная команда: " + command);
        }
    }

    // -- Ниже реализации конкретных команд --

    private void cmdDefine(String[] tokens) {
        if (tokens.length < 3) {
            throw new RuntimeException("Некорректная команда DEFINE (мало аргументов)");
        }
        String name = tokens[1];
        double value = parseOrThrow(tokens[2]);
        defines.put(name, value);
        logger.debug("DEFINE: {} = {}", name, value);
    }

    private void cmdPush(String[] tokens) {
        if (tokens.length < 2) {
            throw new RuntimeException("Некорректная команда PUSH (мало аргументов)");
        }
        double val = getValue(tokens[1]);
        stack.push(val);
        logger.debug("PUSH: {} (текущий размер стека: {})", val, stack.size());
    }

    private void cmdPop() {
        ensureStackSize(1, "POP");
        double removed = stack.pop();
        logger.debug("POP: удалён верхний элемент: {}", removed);
    }

    private void cmdPrint() {
        ensureStackSize(1, "PRINT");
        double top = stack.peek();
        // Печатаем в System.out (а не logger)
        System.out.println(top);
        logger.debug("PRINT: {}", top);
    }

    private void cmdPlus() {
        ensureStackSize(2, "+");
        double b = stack.pop();
        double a = stack.pop();
        double result = a + b;
        stack.push(result);
        logger.debug("{} + {} = {}", a, b, result);
    }

    private void cmdMinus() {
        ensureStackSize(2, "-");
        double b = stack.pop();
        double a = stack.pop();
        double result = a - b;
        stack.push(result);
        logger.debug("{} - {} = {}", a, b, result);
    }

    private void cmdMul() {
        ensureStackSize(2, "*");
        double b = stack.pop();
        double a = stack.pop();
        double result = a * b;
        stack.push(result);
        logger.debug("{} * {} = {}", a, b, result);
    }

    private void cmdDiv() {
        ensureStackSize(2, "/");
        double b = stack.pop();
        double a = stack.pop();
        if (b == 0) {
            throw new ArithmeticException("Деление на ноль");
        }
        double result = a / b;
        stack.push(result);
        logger.debug("{} / {} = {}", a, b, result);
    }

    private void cmdSqrt() {
        ensureStackSize(1, "SQRT");
        double a = stack.pop();
        if (a < 0) {
            throw new ArithmeticException("Невозможно вычислить корень из отрицательного числа: " + a);
        }
        double result = Math.sqrt(a);
        stack.push(result);
        logger.debug("SQRT {} = {}", a, result);
    }

    // -- Вспомогательные методы --

    /**
     * Пытается интерпретировать строку как число.
     * Если это не число, считаем, что это имя ранее определённого параметра.
     */
    private double getValue(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            Double val = defines.get(token);
            if (val == null) {
                throw new RuntimeException("Неизвестный параметр: " + token);
            }
            return val;
        }
    }

    /**
     * Преобразовать строку в число или выбросить исключение.
     */
    private double parseOrThrow(String token) {
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Невозможно преобразовать '" + token + "' в число.");
        }
    }

    /**
     * Проверяет, что в стеке есть нужное число элементов.
     */
    private void ensureStackSize(int size, String command) {
        if (stack.size() < size) {
            throw new RuntimeException("Недостаточно элементов в стеке для команды " + command);
        }
    }

    // -- Геттеры для тестов (если нужно) --

    public Stack<Double> getStack() {
        return stack;
    }

    public Map<String, Double> getDefines() {
        return defines;
    }
}
