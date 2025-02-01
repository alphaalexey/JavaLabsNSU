package ru.alspace;

import java.util.Map;
import java.util.Stack;

/**
 * Контекст исполнения команд: хранит стек чисел и
 * карту названий (DEFINE) -> числовые значения.
 */
public record Context(Stack<Double> stack, Map<String, Double> defines) {
}
