package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Register;

public class OpSetReg extends BitModificationOperation {

    private final Register register;
    private final String toString;

    public OpSetReg(final Clock clock, final Register register, final int bitPosition) {
        super(clock, bitPosition);
        this.register = register;

        this.toString = String.format("set %d, %s", bitPosition, register.name());
    }

    @Override
    public void execute() {
        register.set(set(register.get()));
    }

    @Override
    public String toString() {
        return toString;
    }
}
