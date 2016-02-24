package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.BytePairRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

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
        memory[address] = hlReg.getLow();
        memory[(address + 1) & 0xffff] = hlReg.getHigh();
        return 16;
    }
}
