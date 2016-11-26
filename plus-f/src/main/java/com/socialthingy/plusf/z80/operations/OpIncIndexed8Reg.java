package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpIncIndexed8Reg extends IncOperation {

    private final Register register;

    public OpIncIndexed8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(increment(register.get()));
        return 8;
    }

    @Override
    public String toString() {
        return "inc " + register.name();
    }
}
