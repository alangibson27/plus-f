package com.socialthingy.plusf.z80;

public abstract class Operation {
    protected final Clock clock;

    protected Operation(final Clock clock) {
        this.clock = clock;
    }

    public abstract void execute();
}
