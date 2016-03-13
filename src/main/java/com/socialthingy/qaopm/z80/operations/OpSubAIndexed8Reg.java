package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpSubAIndexed8Reg extends ArithmeticOperation {

    private final Register register;

    public OpSubAIndexed8Reg(final Processor processor, final Register register, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.register = register;
    }

    @Override
    public int execute() {
        accumulator.set(sub(register.get()));
        return 8;
    }
}