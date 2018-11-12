package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Register;

public class OpResReg extends BitModificationOperation {

    private final Register register;
    private final String toString;

    public OpResReg(final Clock clock, final Register register, final int bitPosition) {
        super(clock, bitPosition);
        this.register = register;

        this.toString = String.format("res %d, %s", bitPosition, register.name());
    }

    @Override
    public void execute() {
        register.set(reset(register.get()));
    }

    @Override
    public String toString() {
        return toString;
    }
}
