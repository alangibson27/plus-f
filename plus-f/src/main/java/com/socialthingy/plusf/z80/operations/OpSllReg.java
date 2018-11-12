package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSllReg extends SllOperation {
    private final Register register;

    public OpSllReg(final Processor processor, Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        register.set(shift(register.get()));
        clock.tick(4);
    }

    @Override
    public String toString() {
        return "sll " + register.name();
    }
}
