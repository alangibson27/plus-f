package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitReg extends BitOperation {

    private final Register register;

    public OpBitReg(final Processor processor, final Register register, final int bitPosition) {
        super(processor, bitPosition);
        this.register = register;
    }

    @Override
    public int execute() {
        checkBit(register.get());
        return 8;
    }
}
