package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAndAIndexed8Reg extends AndOperation {

    private final Register register;

    public OpAndAIndexed8Reg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        and(register.get());
    }

    @Override
    public String toString() {
        return "and " + register.name();
    }
}
