package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;

public class Nop extends Operation {
    public Nop(final Clock clock) {
    }

    @Override
    public void execute(final ContentionModel contentionModel, final int initialPcValue, final int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
    }

    @Override
    public String toString() {
        return "nop";
    }
}
