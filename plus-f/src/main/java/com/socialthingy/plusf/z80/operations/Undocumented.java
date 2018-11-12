package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;

public class Undocumented extends Operation {
    private final String description;

    public Undocumented(final String description) {
        super(null);
        this.description = description;
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException(description);
    }
}
