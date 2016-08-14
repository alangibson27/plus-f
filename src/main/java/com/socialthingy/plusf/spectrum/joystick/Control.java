package com.socialthingy.plusf.spectrum.joystick;

public enum Control {
    UP   (0b00001000, '9', '4'),
    DOWN (0b00000100, '8', '3'),
    LEFT (0b00000010, '6', '1'),
    RIGHT(0b00000001, '7', '2'),
    FIRE (0b00010000, '0', '5');

    public final int kempstonValue;
    public final char sinclair1Key;
    public final char sinclair2Key;

    Control(final int kempstonValue, final char sinclair1Key, final char sinclair2Key) {
        this.kempstonValue = kempstonValue;
        this.sinclair1Key = sinclair1Key;
        this.sinclair2Key = sinclair2Key;
    }
}
