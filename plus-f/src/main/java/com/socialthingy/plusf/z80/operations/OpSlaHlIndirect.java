package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpSlaHlIndirect extends SlaOperation {
    private final Register hlReg;
    private final Memory memory;

    public OpSlaHlIndirect(final Processor processor, final Memory memory) {
        super(processor);
        this.hlReg = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        final int address = hlReg.get();
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        contentionModel.applyContention(address, 3);
        contentionModel.applyContention(address, 1);
        contentionModel.applyContention(address, 3);
        final int result = shift(memory.get(address));
        memory.set(address, result);
    }

    @Override
    public String toString() {
        return "sla (hl)";
    }
}
