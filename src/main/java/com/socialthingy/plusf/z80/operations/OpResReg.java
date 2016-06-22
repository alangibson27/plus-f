package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Register;

public class OpResReg extends BitModificationOperation {

    private final Register register;
    private final String toString;

    public OpResReg(final Register register, final int bitPosition) {
        super(bitPosition);
        this.register = register;

        this.toString = String.format("res %d, %s", bitPosition, register.name());
    }

    @Override
    public int execute() {
        register.set(reset(register.get()));
        return 8;
    }

    @Override
    public String toString() {
        return toString;
    }
}
