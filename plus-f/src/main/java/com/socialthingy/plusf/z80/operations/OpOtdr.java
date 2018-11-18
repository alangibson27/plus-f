package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOtdr extends BlockOutOperation {
    public OpOtdr(final Processor processor, final Memory memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(hlReg.get(), 3);
        final int lowByte = cReg.get();
        final int highByte = bReg.get();
        contentionModel.applyIOContention(lowByte, highByte);
        final int bcAddr = bcReg.get();
        decrementBThenWrite(lowByte, highByte, -1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        if (continueLoop()) {
            contentionModel.applyContention(bcAddr, 1);
            contentionModel.applyContention(bcAddr, 1);
            contentionModel.applyContention(bcAddr, 1);
            contentionModel.applyContention(bcAddr, 1);
            contentionModel.applyContention(bcAddr, 1);
        }
    }

    @Override
    public String toString() {
        return "otdr";
    }
}
