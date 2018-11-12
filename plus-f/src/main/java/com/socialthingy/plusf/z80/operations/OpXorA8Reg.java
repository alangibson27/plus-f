package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpXorA8Reg extends XorOperation {

    private final Register register;

    public OpXorA8Reg(final Processor processor, final Clock clock, final Register register) {
        super(processor, clock);
        this.register = register;
    }

    @Override
    public void execute() {
        xor(register.get());
    }

    @Override
    public String toString() {
        return "xor " + register.name();
    }
}
