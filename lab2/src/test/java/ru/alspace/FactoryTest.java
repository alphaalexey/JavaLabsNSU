package ru.alspace;

import org.junit.jupiter.api.Test;
import ru.alspace.commands.PushCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class FactoryTest {
    @Test
    void testFactoryPush() throws CommandExecutionException {
        CommandFactory factory = new CommandFactory(); // Загрузит commands.properties
        Command cmd = factory.createCommand("PUSH"); // Должен создать PushCommand

        // Проверим, что cmd — это PushCommand
        assertInstanceOf(PushCommand.class, cmd);

        // Выполним
        Context ctx = new Context(new Stack<>(), new HashMap<>());
        cmd.execute(ctx, List.of("10"));
        assertEquals(10.0, ctx.stack().peek());
    }
}
