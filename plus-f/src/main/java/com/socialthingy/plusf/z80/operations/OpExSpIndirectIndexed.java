package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpExSpIndirectIndexed extends Operation {

    private final Register spReg;
    private final IndexRegister indexRegister;
    private final Memory memory;

    public OpExSpIndirectIndexed(final Processor processor, final Register indexRegister, final Memory memory) {
        this.spReg = processor.register("sp");
        this.indexRegister = IndexRegister.class.cast(indexRegister);
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int spAddr = spReg.get();
        contentionModel.applyContention(spAddr, 3);
        contentionModel.applyContention(spAddr + 1, 3);
        contentionModel.applyContention(spAddr + 1, 1);
        contentionModel.applyContention(spAddr + 1, 3);
        contentionModel.applyContention(spAddr, 3);
        contentionModel.applyContention(spAddr, 1);
        contentionModel.applyContention(spAddr, 1);

        final int oldIndex = indexRegister.get();

        final int spHigh = 0xffff & (spAddr + 1);
        indexRegister.set(
            Word.from(memory.get(spAddr), memory.get(spHigh))
        );

        memory.set(spAddr, oldIndex & 0x00ff);
        memory.set( spHigh, (oldIndex & 0xff00) >> 8);
    }

    @Override
    public String toString() {
        return "ex (sp), " + indexRegister.name();
    }
}
