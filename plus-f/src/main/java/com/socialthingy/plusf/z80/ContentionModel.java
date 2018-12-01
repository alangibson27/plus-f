package com.socialthingy.plusf.z80;

public abstract class ContentionModel {
    protected final Clock clock;

    public ContentionModel(final Clock clock) {
        this.clock = clock;
    }

    public abstract void applyContention(int address, int baseLength);

    public abstract void applyIOContention(int lowByte, int highByte);
}
