package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpPush16Reg extends Operation {
    private final BytePairRegister register;
    private final Processor processor;

    public OpPush16Reg(final Processor processor, final Clock clock, final Register register) {
        super(clock);
        this.processor = processor;
        this.register = BytePairRegister.class.cast(register);
    }

    @Override
    public void execute() {
        processor.pushByte(register.getHigh());
        processor.pushByte(register.getLow());
        clock.tick(7);
    }

    @Override
    public String toString() {
        return "push " + register.name();
    }
}
