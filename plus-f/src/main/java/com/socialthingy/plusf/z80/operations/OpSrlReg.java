package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSrlReg extends SrlOperation {
    private final Register register;

    public OpSrlReg(final Processor processor, Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(shift(register.get()));
    }

    @Override
    public String toString() {
        return "srl " + register.name();
    }
}
