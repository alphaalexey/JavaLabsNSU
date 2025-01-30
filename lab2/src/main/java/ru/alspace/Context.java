package ru.alspace;

import java.util.Map;
import java.util.Stack;

/**
 * Контекст исполнения команд: хранит стек чисел и
 * карту названий (DEFINE) -> числовые значения.
 */
public class Context {
    private final Stack<Double> stack;
    private final Map<String, Double> defines;

    public Context(Stack<Double> stack, Map<String, Double> defines) {
        this.stack = stack;
        this.defines = defines;
    }

    public Stack<Double> getStack() {
        return stack;
    }

    public Map<String, Double> getDefines() {
        return defines;
    }
}
