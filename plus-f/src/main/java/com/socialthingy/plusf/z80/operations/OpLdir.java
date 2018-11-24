package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpLdir extends BlockOperation {
    public OpLdir(final Processor processor, final Memory memory) {
        super(processor, memory, 1);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(hlReg.get(), 3);
        final int deAddr = deReg.get();
        contentionModel.applyContention(deAddr, 3);
        contentionModel.applyContention(deAddr, 1);
        contentionModel.applyContention(deAddr, 1);
        blockTransfer();
        flagsRegister.set(FlagsRegister.Flag.P, false);
        if (continueLoop()) {
            contentionModel.applyContention(deAddr, 1);
            contentionModel.applyContention(deAddr, 1);
            contentionModel.applyContention(deAddr, 1);
            contentionModel.applyContention(deAddr, 1);
            contentionModel.applyContention(deAddr, 1);
        }
    }

    @Override
    public String toString() {
        return "ldir";
    }
}
