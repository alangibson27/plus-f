package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
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
        final boolean bitSet = checkBit(register.get());
        flagsRegister.set(
            FlagsRegister.Flag.S,
            (bitPosition == 7 || bitPosition == 5 || bitPosition == 3) && bitSet
        );
        return 8;
    }

    @Override
    public String toString() {
        return toString;
    }
}
