package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpExSpIndirectIndexed extends Operation {

    private final Register spReg;
    private final IndexRegister indexRegister;
    private final Memory memory;

    public OpExSpIndirectIndexed(final Processor processor, final Clock clock, final Register indexRegister, final Memory memory) {
        super(clock);
        this.spReg = processor.register("sp");
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.memory = memory;
    }

    @Override
    public void execute() {
        final int oldIndex = indexRegister.get();

        final int spLow = spReg.get();
        final int spHigh = 0xffff & (spLow + 1);
        indexRegister.set(
            Word.from(memory.get(spLow), memory.get(spHigh))
        );

        memory.set( spLow, oldIndex & 0x00ff);
        memory.set( spHigh, (oldIndex & 0xff00) >> 8);
        clock.tick(15);
    }

    @Override
    public String toString() {
        return "ex (sp), " + indexRegister.name();
    }
}
