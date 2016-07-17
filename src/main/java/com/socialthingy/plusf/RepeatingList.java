package com.socialthingy.plusf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;

public class RepeatingList<T> implements Iterable<T> {
    private Queue<Repetition> items = new LinkedList<>();

    public void add(final T item, final int repetitions) {
        if (repetitions > 0) {
            items.add(new Repetition(item, repetitions));
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new RepetitionIterator();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }

    private class Repetition {
        private final T item;
        private final int repetitions;

        private Repetition(final T item, final int repetitions) {
            this.item = item;
            this.repetitions = repetitions;
        }

        public String toString() {
            return String.format("%s * %d", item.toString(), repetitions);
        }
    }

    private class RepetitionIterator implements Iterator<T> {
        private int count;
        private T item;

        private RepetitionIterator() {
            nextItem();
        }

        @Override
        public boolean hasNext() {
            return count > 0 || !items.isEmpty();
        }

        @Override
        public T next() {
            final T toReturn = item;
            count--;
            nextItem();
            return toReturn;
        }

        private void nextItem() {
            if (count == 0 && !items.isEmpty()) {
                final Repetition nextRep = items.remove();
                item = nextRep.item;
                count = nextRep.repetitions;
            }
        }
    }
}
