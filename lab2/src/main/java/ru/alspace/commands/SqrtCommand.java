package ru.alspace.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * SQRT
 * Снимает со стека 1 число, вычисляет корень, возвращает в стек результат.
 */
public class SqrtCommand extends Command {
    private static final Logger logger = LogManager.getLogger(SqrtCommand.class);

    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // SQRT не принимает аргументов
        if (!args.isEmpty()) {
            throw new CommandExecutionException("SQRT: не должно быть аргументов, а получено " + args.size());
        }

        Stack<Double> stack = context.stack();
        if (stack.isEmpty()) {
            throw new CommandExecutionException("SQRT: в стеке нет элементов");
        }

        double value = stack.pop();
        if (value < 0) {
            stack.push(value);
            throw new CommandExecutionException("SQRT: корень из отрицательного числа: " + value);
        }

        double result = Math.sqrt(value);
        stack.push(result);
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            logger.warn("SQRT: результат не является действительным числом");
            System.out.println("При вычислении квадратного корня произошло переполнение, дальнейшие вычисления могут быть некорректны");
        }
    }
}
