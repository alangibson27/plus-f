package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;

public class Undocumented extends Operation {
    private final String description;

    public Undocumented(final String description) {
        this.description = description;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        throw new UnsupportedOperationException(description);
    }
}
