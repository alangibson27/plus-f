package com.socialthingy.plusf;

import com.google.common.collect.Iterators;

import java.util.Iterator;

public class IteratorIterator<T> implements Iterator<T> {
    private final Iterator<Iterator<T>> allIterators;
    private Iterator<T> currentIterator;

    public IteratorIterator(final Iterator<T> ... iterators) {
        if (iterators.length == 0) {
            throw new IllegalArgumentException();
        }
        this.allIterators = Iterators.forArray(iterators);
        this.currentIterator = this.allIterators.next();
    }

    @Override
    public boolean hasNext() {
        return this.currentIterator.hasNext();
    }

    @Override
    public T next() {
        final T value = currentIterator.next();
        if (!currentIterator.hasNext() & allIterators.hasNext()) {
            currentIterator = allIterators.next();
        }
        return value;
    }
}
