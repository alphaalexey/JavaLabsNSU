package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * SQRT
 * Снимает со стека 1 число, вычисляет корень, возвращает в стек результат.
 */
public class SqrtCommand implements Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // SQRT не принимает аргументов
        if (!args.isEmpty()) {
            throw new CommandExecutionException("SQRT: не должно быть аргументов, а получено " + args.size());
        }

        Stack<Double> stack = context.getStack();
        if (stack.isEmpty()) {
            throw new CommandExecutionException("SQRT: в стеке нет элементов");
        }

        double value = stack.pop();
        if (value < 0) {
            throw new CommandExecutionException("SQRT: корень из отрицательного числа: " + value);
        }
        stack.push(Math.sqrt(value));
    }
}
