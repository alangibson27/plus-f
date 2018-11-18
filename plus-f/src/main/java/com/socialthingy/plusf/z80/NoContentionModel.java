package com.socialthingy.plusf.z80;

public class NoContentionModel extends ContentionModel {
    public NoContentionModel(Clock clock) {
        super(clock);
    }

    @Override
    public void applyContention(int address, int baseLength) {
        clock.tick(baseLength);
    }

    @Override
    public void applyIOContention(final int lowByte, final int highByte) {
        clock.tick(4);
    }
}
