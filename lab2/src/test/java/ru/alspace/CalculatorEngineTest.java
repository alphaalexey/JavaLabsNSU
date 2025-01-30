package ru.alspace;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

public class CalculatorEngineTest {
    @Test
    void testDefineAndPush() {
        CalculatorEngine engine = new CalculatorEngine();
        // Выполняем команду DEFINE a 4
        engine.executeCommand("DEFINE", new String[]{"DEFINE", "a", "4"});
        // Выполняем команду PUSH a
        engine.executeCommand("PUSH", new String[]{"PUSH", "a"});

        Stack<Double> stack = engine.getStack();

        assertFalse(stack.isEmpty());
        assertEquals(4.0, stack.peek(), 1e-9);
        assertEquals(4.0, engine.getDefines().get("a"));
    }

    @Test
    void testPlus() {
        CalculatorEngine engine = new CalculatorEngine();
        engine.executeCommand("PUSH", new String[]{"PUSH", "2"});
        engine.executeCommand("PUSH", new String[]{"PUSH", "3"});
        engine.executeCommand("+", new String[]{"+"});

        Stack<Double> stack = engine.getStack();
        assertEquals(1, stack.size());
        assertEquals(5.0, stack.peek());
    }

    @Test
    void testDivisionByZero() {
        CalculatorEngine engine = new CalculatorEngine();
        engine.executeCommand("PUSH", new String[]{"PUSH", "10"});
        engine.executeCommand("PUSH", new String[]{"PUSH", "0"});

        assertThrows(ArithmeticException.class, () ->
                engine.executeCommand("/", new String[]{"/"})
        );
    }

    @Test
    void testSqrtNegative() {
        CalculatorEngine engine = new CalculatorEngine();
        engine.executeCommand("PUSH", new String[]{"PUSH", "-9"});
        assertThrows(ArithmeticException.class, () ->
                engine.executeCommand("SQRT", new String[]{"SQRT"})
        );
    }

    @Test
    void testPrint() {
        // Тест проверяет, что PRINT корректно выводит значение.
        // Для этого перехватим System.out.
        CalculatorEngine engine = new CalculatorEngine();
        engine.executeCommand("PUSH", new String[]{"PUSH", "9"});

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(outContent));

        engine.executeCommand("PRINT", new String[]{"PRINT"});

        System.setOut(new java.io.PrintStream(new FileOutputStream(java.io.FileDescriptor.out)));

        String output = outContent.toString().trim();
        assertEquals("9.0", output);
    }
}
