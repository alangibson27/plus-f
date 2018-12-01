package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;

public class OpRet extends RetOperation {
    public OpRet(final Processor processor) {
        super(processor);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        final int sp = processor.register("sp").get();
        contentionModel.applyContention(sp, 3);
        contentionModel.applyContention(sp + 1, 3);
        ret();
    }

    @Override
    public String toString() {
        return "ret";
    }
}
