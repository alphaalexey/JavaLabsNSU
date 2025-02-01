package ru.alspace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.alspace.commands.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Набор тестов для проверок всех команд, включая проверки
 * на неправильное количество аргументов (избыточные).
 */
public class CommandsTest {

    private Context context;

    @BeforeEach
    void setUp() {
        // Перед каждым тестом — чистый контекст (пустой стек, пустой map).
        context = new Context(new Stack<>(), new HashMap<>());
    }

    // ------------------------------------------------------------
    // DEFINE
    // ------------------------------------------------------------
    @Test
    void testDefineOk() throws CommandExecutionException {
        // DEFINE a 10
        Command cmd = new DefineCommand();
        cmd.execute(context, List.of("a", "10"));

        // Проверим, что в defines лежит a=10.0
        assertEquals(10.0, context.defines().get("a"), 1e-9);
    }

    @Test
    void testDefineWrongArgsTooFew() {
        // DEFINE (только 1 аргумент)
        Command cmd = new DefineCommand();
        assertThrows(CommandExecutionException.class, () ->
                cmd.execute(context, List.of("a")) // нужно 2, а тут 1
        );
    }

    @Test
    void testDefineWrongArgsTooMany() {
        // DEFINE (3 аргумента)
        Command cmd = new DefineCommand();
        assertThrows(CommandExecutionException.class, () ->
                cmd.execute(context, List.of("a", "10", "extra"))
        );
    }

    // ------------------------------------------------------------
    // PUSH
    // ------------------------------------------------------------
    @Test
    void testPushNumber() throws CommandExecutionException {
        // PUSH 3.14
        Command cmd = new PushCommand();
        cmd.execute(context, List.of("3.14"));
        assertEquals(1, context.stack().size());
        assertEquals(3.14, context.stack().peek(), 1e-9);
    }

    @Test
    void testPushUnknownVar() {
        // PUSH x, которая не определена
        Command cmd = new PushCommand();
        assertThrows(CommandExecutionException.class, () ->
                cmd.execute(context, List.of("x"))
        );
    }

    @Test
    void testPushTooManyArgs() {
        // PUSH не может принимать >1 аргумента
        Command cmd = new PushCommand();
        assertThrows(CommandExecutionException.class, () ->
                cmd.execute(context, List.of("arg1", "arg2"))
        );
    }

    // ------------------------------------------------------------
    // POP (0 или 1 аргумент)
    // ------------------------------------------------------------
    @Test
    void testPopNoArgs() throws CommandExecutionException {
        // Положим 2 значения в стек
        context.stack().push(10.0);
        context.stack().push(20.0);

        Command popCmd = new PopCommand();
        popCmd.execute(context, List.of());

        // Удалён верхний элемент (20.0)
        assertEquals(1, context.stack().size());
        assertEquals(10.0, context.stack().peek());
    }

    @Test
    void testPopStoreVariable() throws CommandExecutionException {
        // В стеке [10.0, 20.0]
        context.stack().push(10.0);
        context.stack().push(20.0);

        // POP x
        Command popCmd = new PopCommand();
        popCmd.execute(context, List.of("x")); // 1 аргумент

        // Остался один элемент (10.0), и x=20.0
        assertEquals(10.0, context.stack().peek(), 1e-9);
        assertEquals(20.0, context.defines().get("x"), 1e-9);
    }

    @Test
    void testPopEmptyStack() {
        Command popCmd = new PopCommand();
        // Пустой стек → ошибка
        assertThrows(CommandExecutionException.class, () ->
                popCmd.execute(context, List.of())
        );
    }

    @Test
    void testPopTooManyArgs() {
        Command popCmd = new PopCommand();
        // Если аргументов >1 → ошибка
        assertThrows(CommandExecutionException.class, () ->
                popCmd.execute(context, List.of("a", "b"))
        );
    }

    // ------------------------------------------------------------
    // PRINT (ровно 0 аргументов)
    // ------------------------------------------------------------
    @Test
    void testPrintOk() throws CommandExecutionException {
        context.stack().push(9.0);

        // Перехватим вывод
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(bos));

        try {
            Command printCmd = new PrintCommand();
            printCmd.execute(context, List.of());

            String output = bos.toString().trim();
            assertEquals("9.0", output);
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    void testPrintEmptyStack() {
        Command printCmd = new PrintCommand();
        assertThrows(CommandExecutionException.class, () ->
                printCmd.execute(context, List.of())
        );
    }

    @Test
    void testPrintTooManyArgs() {
        Command printCmd = new PrintCommand();
        assertThrows(CommandExecutionException.class, () ->
                printCmd.execute(context, List.of("extraArg"))
        );
    }

    // ------------------------------------------------------------
    // +
    // ------------------------------------------------------------
    @Test
    void testAddOk() throws CommandExecutionException {
        // Стек: [2, 3], делаем +
        context.stack().push(2.0);
        context.stack().push(3.0);

        Command addCmd = new AddCommand();
        addCmd.execute(context, List.of());
        // Стек: [5]
        assertEquals(5.0, context.stack().peek(), 1e-9);
    }

    @Test
    void testAddNotEnough() {
        context.stack().push(2.0);  // всего 1
        Command addCmd = new AddCommand();
        assertThrows(CommandExecutionException.class, () ->
                addCmd.execute(context, List.of())
        );
    }

    @Test
    void testAddTooManyArgs() {
        Command addCmd = new AddCommand();
        assertThrows(CommandExecutionException.class, () ->
                addCmd.execute(context, List.of("extra"))
        );
    }

    // ------------------------------------------------------------
    // -
    // ------------------------------------------------------------
    @Test
    void testSubOk() throws CommandExecutionException {
        // Стек: [10, 4], SUB => 10-4=6
        context.stack().push(10.0);
        context.stack().push(4.0);

        Command subCmd = new SubCommand();
        subCmd.execute(context, List.of());
        assertEquals(6.0, context.stack().peek(), 1e-9);
    }

    @Test
    void testSubNotEnough() {
        context.stack().push(10.0); // 1 элемент
        Command subCmd = new SubCommand();
        assertThrows(CommandExecutionException.class, () ->
                subCmd.execute(context, List.of())
        );
    }

    @Test
    void testSubTooManyArgs() {
        Command subCmd = new SubCommand();
        assertThrows(CommandExecutionException.class, () ->
                subCmd.execute(context, List.of("extra"))
        );
    }

    // ------------------------------------------------------------
    // *
    // ------------------------------------------------------------
    @Test
    void testMulOk() throws CommandExecutionException {
        // Стек: [2, 5], * => 10
        context.stack().push(2.0);
        context.stack().push(5.0);

        Command mulCmd = new MulCommand();
        mulCmd.execute(context, List.of());
        assertEquals(10.0, context.stack().peek(), 1e-9);
    }

    @Test
    void testMulNotEnough() {
        context.stack().push(2.0);
        Command mulCmd = new MulCommand();
        assertThrows(CommandExecutionException.class, () ->
                mulCmd.execute(context, List.of())
        );
    }

    @Test
    void testMulTooManyArgs() {
        Command mulCmd = new MulCommand();
        assertThrows(CommandExecutionException.class, () ->
                mulCmd.execute(context, List.of("extra"))
        );
    }

    // ------------------------------------------------------------
    // /
    // ------------------------------------------------------------
    @Test
    void testDivOk() throws CommandExecutionException {
        // Стек: [9, 3], / => 3
        context.stack().push(9.0);
        context.stack().push(3.0);

        Command divCmd = new DivCommand();
        divCmd.execute(context, List.of());
        assertEquals(3.0, context.stack().peek(), 1e-9);
    }

    @Test
    void testDivZero() {
        // [5, 0]
        context.stack().push(5.0);
        context.stack().push(0.0);

        Command divCmd = new DivCommand();
        assertThrows(CommandExecutionException.class, () ->
                divCmd.execute(context, List.of())
        );
    }

    @Test
    void testDivNotEnough() {
        context.stack().push(5.0); // всего 1
        Command divCmd = new DivCommand();
        assertThrows(CommandExecutionException.class, () ->
                divCmd.execute(context, List.of())
        );
    }

    @Test
    void testDivTooManyArgs() {
        Command divCmd = new DivCommand();
        assertThrows(CommandExecutionException.class, () ->
                divCmd.execute(context, List.of("extra"))
        );
    }

    // ------------------------------------------------------------
    // SQRT
    // ------------------------------------------------------------
    @Test
    void testSqrtOk() throws CommandExecutionException {
        context.stack().push(16.0);
        Command sqrtCmd = new SqrtCommand();
        sqrtCmd.execute(context, List.of());
        assertEquals(4.0, context.stack().peek(), 1e-9);
    }

    @Test
    void testSqrtNegative() {
        context.stack().push(-9.0);
        Command sqrtCmd = new SqrtCommand();
        assertThrows(CommandExecutionException.class, () ->
                sqrtCmd.execute(context, List.of())
        );
    }

    @Test
    void testSqrtEmptyStack() {
        Command sqrtCmd = new SqrtCommand();
        assertThrows(CommandExecutionException.class, () ->
                sqrtCmd.execute(context, List.of())
        );
    }

    @Test
    void testSqrtTooManyArgs() {
        Command sqrtCmd = new SqrtCommand();
        assertThrows(CommandExecutionException.class, () ->
                sqrtCmd.execute(context, List.of("extra"))
        );
    }
}
