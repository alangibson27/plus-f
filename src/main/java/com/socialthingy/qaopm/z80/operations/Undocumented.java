package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;

public class Undocumented implements Operation {
    private final String description;

    public Undocumented(final String description) {
        this.description = description;
    }

    @Override
    public int execute() {
        throw new UnsupportedOperationException(description);
    }
}
