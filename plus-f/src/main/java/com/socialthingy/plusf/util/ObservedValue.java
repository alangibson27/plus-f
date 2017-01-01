package com.socialthingy.plusf.util;

import java.util.Objects;
import java.util.Observable;

public class ObservedValue<T> extends Observable {
    private T value;

    public ObservedValue(final T initial) {
        this.value = initial;
    }

    public T get() {
        return value;
    }

    public void set(final T newValue) {
        if (!Objects.equals(value, newValue)) {
            this.value = newValue;
            setChanged();
            notifyObservers();
        }
    }
}
