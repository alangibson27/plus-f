package com.socialthingy.plusf.tape;

public class SignalState {
    private boolean high;

    public SignalState(final boolean high) {
        this.high = high;
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
