package ru.mai.lessons.rpks.javawebbrowser.commons;

import java.util.Objects;

public class Pair<T, U> {

    private T key;
    private U value;

    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public U getValue() {
        return value;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public void setValue(U value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> other) {
            return Objects.equals(other.key, key) && Objects.equals(other.value, value);
        }
        return false;
    }

    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<T, U>(first, second);
    }
}
