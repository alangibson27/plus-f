package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpRlcReg extends RotateOperation {
    private final Register register;

    public OpRlcReg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        final int result = rlcValue(register.get());
        setSignZeroAndParity(result);
        register.set(result);
    }

    @Override
    public String toString() {
        return "rlc " + register.name();
    }
}
