package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Register;

public class OpResReg extends BitModificationOperation {

    private final Register register;

    public OpResReg(final Register register, final int bitPosition) {
        super(bitPosition);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(reset(register.get()));
        return 8;
    }
}
