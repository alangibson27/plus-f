package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdAddressHl extends Operation {
    private final Processor processor;
    private final BytePairRegister hlReg;
    private final Memory memory;

    public OpLdAddressHl(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.processor = processor;
        this.hlReg = BytePairRegister.class.cast(processor.register("hl"));
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int address = processor.fetchNextWord();
        memory.set(address, hlReg.getLow());
        memory.set(address + 1, hlReg.getHigh());
        clock.tick(12);
    }

    @Override
    public String toString() {
        return "ld (nn), hl";
    }

}
