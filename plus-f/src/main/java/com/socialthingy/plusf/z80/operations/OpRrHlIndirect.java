package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpRrHlIndirect extends RotateOperation {

    private final Memory memory;
    private final Register hlReg;

    public OpRrHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.memory = memory;
        this.hlReg = processor.register("hl");
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = rrValue(memory.get(address));
        setSignZeroAndParity(result);
        memory.set( address, result);
    }

    @Override
    public String toString() {
        return "rr (hl)";
    }
}
