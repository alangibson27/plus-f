package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpCpir extends BlockOperation {
    public OpCpir(final Processor processor, final Memory memory) {
        super(processor, memory, 1);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int hlAddr = hlReg.get();
        contentionModel.applyContention(hlAddr, 3);
        contentionModel.applyContention(hlAddr, 1);
        contentionModel.applyContention(hlAddr, 1);
        contentionModel.applyContention(hlAddr, 1);
        contentionModel.applyContention(hlAddr, 1);
        contentionModel.applyContention(hlAddr, 1);
        if (blockCompare() > 0) {
            contentionModel.applyContention(hlAddr, 1);
            contentionModel.applyContention(hlAddr, 1);
            contentionModel.applyContention(hlAddr, 1);
            contentionModel.applyContention(hlAddr, 1);
            contentionModel.applyContention(hlAddr, 1);
            super.continueLoop();
        }
    }

    @Override
    public String toString() {
        return "cpir";
    }
}
