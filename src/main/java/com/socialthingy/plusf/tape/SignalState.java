package com.socialthingy.plusf.tape;

public class SignalState {
    public enum Adjustment {
        SET_HIGH, SET_LOW, FLIP, NO_CHANGE;
    }

    private boolean high;

    public SignalState(final boolean high) {
        this.high = high;
    }

    public void adjust(final Adjustment adjustment) {
        switch (adjustment) {
            case SET_HIGH: set(true); break;
            case SET_LOW: set(false); break;
            case FLIP: flip(); break;
        }
    }

    public void set(final boolean high) {
        this.high = high;
    }

    public boolean flip() {
        high = !high;
        return high;
    }

    public boolean get() {
        return this.high;
    }
}
