package com.socialthingy.plusf;

import com.socialthingy.replist.RepList;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class RepeatingList<T> implements Iterable<T> {
    private Queue<Repetition> items = new LinkedList<>();

    public void append(final RepeatingList<T> other) {
        other.items.forEach(this.items::add);
    }

    public void add(final T item, final int repetitions) {
        if (repetitions > 0) {
            items.add(new Repetition(item, repetitions));
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new RepetitionIterator();
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
