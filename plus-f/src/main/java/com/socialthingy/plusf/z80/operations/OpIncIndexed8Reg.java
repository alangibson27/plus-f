package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpIncIndexed8Reg extends IncOperation {

    private final Register register;

    public OpIncIndexed8Reg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(increment(register.get()));
    }

    @Override
    public String toString() {
        return "inc " + register.name();
    }
}
