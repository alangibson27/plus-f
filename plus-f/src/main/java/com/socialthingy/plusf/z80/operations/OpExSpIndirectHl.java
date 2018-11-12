package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpExSpIndirectHl extends Operation {
    private final Register spReg;
    private final Register hReg;
    private final Register lReg;
    private final Memory memory;

    public OpExSpIndirectHl(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.spReg = processor.register("sp");
        this.hReg = processor.register("h");
        this.lReg = processor.register("l");
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int oldH = hReg.get();
        final int oldL = lReg.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        lReg.set(memory.get(spLow));
        hReg.set(memory.get(spHigh));

        memory.set( spLow, oldL);
        memory.set( spHigh, oldH);
        clock.tick(15);
    }

    @Override
    public String toString() {
        return "ex (sp), hl";
    }
}
