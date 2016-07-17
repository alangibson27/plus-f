package com.socialthingy.plusf.tape;

public class StopTapeBit extends TapeBlock.Bit {
    public static StopTapeBit INSTANCE = new StopTapeBit();

    private StopTapeBit() {
        super(false, "stop tape");
    }
}
