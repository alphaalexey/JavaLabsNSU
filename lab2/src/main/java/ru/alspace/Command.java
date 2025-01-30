package ru.alspace;

import java.util.List;

/**
 * Общий интерфейс для всех команд калькулятора.
 */
public interface Command {
    /**
     * Выполняет команду, используя контекст (стек, map) и аргументы.
     *
     * @param context контекст исполнения (стек, map)
     * @param args    список аргументов (все токены, которые идут после названия команды)
     * @throws CommandExecutionException если при исполнении произошла ошибка
     */
    void execute(Context context, List<String> args) throws CommandExecutionException;
}
