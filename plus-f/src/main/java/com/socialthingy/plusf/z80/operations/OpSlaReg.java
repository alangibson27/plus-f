package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSlaReg extends SlaOperation {
    private final Register register;

    public OpSlaReg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(shift(register.get()));
    }

    @Override
    public String toString() {
        return "sla " + register.name();
    }
}
