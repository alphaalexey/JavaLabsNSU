package ru.alspace.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * /
 * Снимает со стека 2 числа (a, b), вычисляет a / b.
 */
public class DivCommand extends Command {
    private static final Logger logger = LogManager.getLogger(DivCommand.class);

    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // / не принимает аргументов
        if (!args.isEmpty()) {
            throw new CommandExecutionException("/ : не должно быть аргументов, а получено " + args.size());
        }

        Stack<Double> stack = context.stack();
        if (stack.size() < 2) {
            throw new CommandExecutionException("/ : в стеке меньше 2 элементов");
        }

        double b = stack.pop();
        double a = stack.pop();
        if (b == 0) {
            stack.push(a);
            stack.push(b);
            throw new CommandExecutionException("/ : деление на ноль");
        }

        double result = a / b;
        stack.push(result);
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            logger.warn("/: результат не является действительным числом");
            System.out.println("При делении произошло переполнение, дальнейшие вычисления могут быть некорректны");
        }
    }
}
