package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdAddress16Reg extends Operation {
    private final Processor processor;
    private final Register register;
    private final Memory memory;

    public OpLdAddress16Reg(final Processor processor, final Clock clock, final Memory memory, final Register register) {
        super(clock);
        this.processor = processor;
        this.register = register;
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = processor.fetchNextWord();
        final int value = register.get();
        memory.set( address, value & 0x00ff);
        memory.set( (address + 1) & 0xffff, (value & 0xff00) >> 8);
    }

    @Override
    public String toString() {
        return String.format("ld (nn), %s", register.name());
    }
}
