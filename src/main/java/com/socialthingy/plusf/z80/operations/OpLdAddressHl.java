package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.BytePairRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpLdAddressHl implements Operation {
    private final Processor processor;
    private final BytePairRegister hlReg;
    private final int[] memory;

    public OpLdAddressHl(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
        this.memory = memory;
    }

    @Override
    public int execute() {
        final int address = processor.fetchNextWord();
        Memory.set(memory, address, hlReg.getLow());
        Memory.set(memory, (address + 1) & 0xffff, hlReg.getHigh());
        return 16;
    }
}
