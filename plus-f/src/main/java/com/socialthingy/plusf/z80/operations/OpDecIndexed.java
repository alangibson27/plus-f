package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpDecIndexed extends Operation {

    private final Register register;

    public OpDecIndexed(final Clock clock, final Register register) {
        super(clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(register.get() - 1);
        clock.tick(2);
    }

    @Override
    public String toString() {
        return "dec " + register.name();
    }
}
