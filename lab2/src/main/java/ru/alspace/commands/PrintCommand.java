package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * PRINT
 * Выводит (System.out) верхний элемент стека (без удаления).
 */
public class PrintCommand extends Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // PRINT не принимает аргументов
        if (!args.isEmpty()) {
            throw new CommandExecutionException("PRINT: не должно быть аргументов, а получено " + args.size());
        }

        Stack<Double> stack = context.stack();
        if (stack.isEmpty()) {
            throw new CommandExecutionException("PRINT: стек пуст");
        }
        // Выводим в консоль
        double top = stack.peek();
        System.out.println(top);
    }
}
