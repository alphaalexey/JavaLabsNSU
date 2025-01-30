package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;
import java.util.Stack;

/**
 * POP
 * - без аргументов: снять верхний элемент со стека
 * - c одним аргументом (имя): снять верхний элемент и записать его в defines[имя].
 */
public class PopCommand implements Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // Допускаем 0 или 1 аргумент
        if (args.size() > 1) {
            // В реальности args.size() < 0 не может случиться, но оставим логическую формулировку
            throw new CommandExecutionException("POP: Usage - 'POP' или 'POP <varName>' (получено " + args.size() + " арг.)");
        }

        Stack<Double> stack = context.getStack();
        if (stack.isEmpty()) {
            throw new CommandExecutionException("POP: стек пуст");
        }

        // Снимаем верхний элемент
        double top = stack.pop();

        // Если есть один аргумент, считаем его именем переменной
        if (args.size() == 1) {
            String varName = args.getFirst();
            context.getDefines().put(varName, top);
        }
    }
}
