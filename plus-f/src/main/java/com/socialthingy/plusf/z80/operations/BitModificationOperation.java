package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;

abstract class BitModificationOperation extends Operation {
    private final int bitPosition;

    protected BitModificationOperation(final int bitPosition) {
        this.bitPosition = bitPosition;
    }

    protected int reset(final int value) {
        return value & (0xff - (1 << bitPosition));
    }

    protected int set(final int value) {
        return value | (1 << bitPosition);
    }
}
