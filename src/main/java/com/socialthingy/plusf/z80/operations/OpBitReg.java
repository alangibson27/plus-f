package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitReg extends BitOperation {

    private final Register register;
    private final String toString;

    public OpBitReg(final Processor processor, final Register register, final int bitPosition) {
        super(processor, bitPosition);
        this.register = register;

        this.toString = String.format("bit %d, %s", bitPosition, register.name());
    }

    @Override
    public int execute() {
        checkBit(register.get());
        return 8;
    }

    @Override
    public String toString() {
        return toString;
    }
}
