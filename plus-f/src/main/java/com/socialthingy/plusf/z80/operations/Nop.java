package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;

public class Nop extends Operation {
    public Nop(final Clock clock) {
        super(clock);
    }

    @Override
    public void execute() {
    }

    @Override
    public String toString() {
        return "nop";
    }
}
