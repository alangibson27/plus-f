package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpAIndexed8Reg extends ArithmeticOperation {

    private final Register register;

    public OpCpAIndexed8Reg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock, false);
        this.register = register;
    }

    @Override
    public void execute() {
        sub(register.get(), false);
    }

    @Override
    public String toString() {
        return "cp " + register.name();
    }
}
