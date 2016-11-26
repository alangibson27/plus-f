package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddA8Reg extends ArithmeticOperation {

    private final Register register;
    private final String toString;

    public OpAddA8Reg(final Processor processor, final Register register, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.register = register;

        this.toString = (useCarryFlag ? "adc a, " : "add a, ") + register.name();
    }

    @Override
    public int execute() {
        add(register.get());
        return 4;
    }

    @Override
    public String toString() {
        return toString;
    }
}