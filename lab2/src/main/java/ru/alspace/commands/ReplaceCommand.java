package ru.alspace.commands;

import ru.alspace.Command;
import ru.alspace.CommandExecutionException;
import ru.alspace.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * REPLACE
 * Удаляет все числа из стека и заменяет их переданными числами или переменными.
 * Если в аргументах присутствует переменная, её значение берётся из контекста (map),
 * предварительно проверяя, что она определена.
 * Если какая-либо переменная не определена, стек остаётся неизменным.
 * <p>
 * Пример использования: REPLACE 1 2 3 n
 */
public class ReplaceCommand extends Command {
    @Override
    public void execute(Context context, List<String> args) throws CommandExecutionException {
        if (args.isEmpty()) {
            throw new CommandExecutionException("REPLACE: ожидаются аргументы, а получено 0");
        }

        Map<String, Double> variables = context.defines();
        List<Double> replacements = new ArrayList<>();

        for (String token : args) {
            try {
                // Если можно преобразовать в число, то используем число
                double value = Double.parseDouble(token);
                replacements.add(value);
            } catch (NumberFormatException e) {
                // Предполагаем, что это имя переменной.
                if (!variables.containsKey(token)) {
                    // Стек остаётся без изменений.
                    throw new CommandExecutionException("REPLACE: переменная '" + token + "' не определена");
                }
                replacements.add(variables.get(token));
            }
        }

        // Если все аргументы корректны, очищаем стек от старых чисел.
        Stack<Double> stack = context.stack();
        stack.clear();

        // Помещаем полученные значения в стек.
        for (Double d : replacements) {
            stack.push(d);
        }
    }
}
