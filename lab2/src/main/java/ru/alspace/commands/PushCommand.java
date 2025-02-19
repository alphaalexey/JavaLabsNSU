package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.List;

/**
 * PUSH <value-or-name>
 */
public class PushCommand extends Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        if (args.size() != 1) {
            throw new CommandExecutionException(
                    "PUSH: требуется ровно 1 аргумент (число или имя), а получено " + args.size());
        }
        String token = args.getFirst();

        double val;
        // Сначала пробуем интерпретировать как число
        try {
            val = Double.parseDouble(token);
        } catch (NumberFormatException e) {
            // Если не число, считаем, что это имя
            Double defVal = context.defines().get(token);
            if (defVal == null) {
                throw new CommandExecutionException("PUSH: неизвестный параметр '" + token + "'");
            }
            val = defVal;
        }

        context.stack().push(val);
    }
}
