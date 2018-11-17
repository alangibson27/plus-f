package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpExSpIndirectHl extends Operation {
    private final Register spReg;
    private final Register hReg;
    private final Register lReg;
    private final Memory memory;

    public OpExSpIndirectHl(final Processor processor, final Memory memory) {
        this.spReg = processor.register("sp");
        this.hReg = processor.register("h");
        this.lReg = processor.register("l");
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        final int spAddr = spReg.get();
        contentionModel.applyContention(spAddr, 3);
        contentionModel.applyContention(spAddr + 1, 3);
        contentionModel.applyContention(spAddr + 1, 1);
        contentionModel.applyContention(spAddr + 1, 3);
        contentionModel.applyContention(spAddr, 3);
        contentionModel.applyContention(spAddr, 1);
        contentionModel.applyContention(spAddr, 1);
        final int oldH = hReg.get();
        final int oldL = lReg.get();

        final int spHigh = 0xffff & (spAddr + 1);
        lReg.set(memory.get(spAddr));
        hReg.set(memory.get(spHigh));

        memory.set(spAddr, oldL);
        memory.set(spHigh, oldH);
    }

    @Override
    public String toString() {
        return "ex (sp), hl";
    }
}
