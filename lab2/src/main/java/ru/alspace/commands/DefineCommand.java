package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;

/**
 * DEFINE <name> <value>
 */
public class DefineCommand extends Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        // Нужно ровно 2 аргумента: name, value
        if (args.size() != 2) {
            throw new CommandExecutionException(
                    "DEFINE: требуется ровно 2 аргумента (имя и значение), а получено " + args.size());
        }

        String name = args.get(0);
        if (Character.isDigit(name.charAt(0))) {
            throw new CommandExecutionException("DEFINE: имя не может начинаться с числа");
        }

        String valueStr = args.get(1);
        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            throw new CommandExecutionException("DEFINE: не удалось преобразовать " + valueStr + " в число");
        }

        // Записываем в map
        context.defines().put(name, value);
    }
}
