package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpLdi extends BlockOperation {
    public OpLdi(final Processor processor, final Memory memory) {
        super(processor, memory, 1);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(hlReg.get(), 3);
        contentionModel.applyContention(deReg.get(), 3);
        contentionModel.applyContention(deReg.get(), 1);
        contentionModel.applyContention(deReg.get(), 1);

        blockTransfer();
    }

    @Override
    public String toString() {
        return "ldi";
    }
}
