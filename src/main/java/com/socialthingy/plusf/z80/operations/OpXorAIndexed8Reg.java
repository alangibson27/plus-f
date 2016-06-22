package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpXorAIndexed8Reg extends XorOperation {

    private final Register register;

    public OpXorAIndexed8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        xor(register.get());
        return 8;
    }

    @Override
    public String toString() {
        return "xor " + register.name();
    }
}
