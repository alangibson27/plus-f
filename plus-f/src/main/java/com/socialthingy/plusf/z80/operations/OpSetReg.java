package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Register;

public class OpSetReg extends BitModificationOperation {

    private final Register register;
    private final String toString;

    public OpSetReg(final Register register, final int bitPosition) {
        super(bitPosition);
        this.register = register;

        this.toString = String.format("set %d, %s", bitPosition, register.name());
    }

    @Override
    public int execute() {
        register.set(set(register.get()));
        return 8;
    }

    @Override
    public String toString() {
        return toString;
    }
}
