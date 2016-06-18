package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdAddress16Reg implements Operation {
    private final Processor processor;
    private final Register register;
    private final int[] memory;

    public OpLdAddress16Reg(final Processor processor, final int[] memory, final Register register) {
        this.processor = processor;
        this.register = register;
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = processor.fetchNextWord();
        final int value = register.get();
        Memory.set(memory, address, value & 0x00ff);
        Memory.set(memory, (address + 1) & 0xffff, (value & 0xff00) >> 8);
        return 20;
    }

    @Override
    public String toString() {
        return String.format("ld (nn), %s", register.name());
    }
}
