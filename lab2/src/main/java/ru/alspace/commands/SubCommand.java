package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * -
 * Снимает со стека 2 числа (a, b), выполняет (a - b).
 * a — это то, что было ниже в стеке, b — верхний элемент.
 */
public class SubCommand extends Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // - не принимает аргументов
        if (!args.isEmpty()) {
            throw new CommandExecutionException("- : не должно быть аргументов, а получено " + args.size());
        }

        Stack<Double> stack = context.stack();
        if (stack.size() < 2) {
            throw new CommandExecutionException("- : в стеке меньше 2 элементов");
        }

        double b = stack.pop();
        double a = stack.pop();
        stack.push(a - b);
    }
}
