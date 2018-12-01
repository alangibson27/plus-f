package com.socialthingy.plusf.spectrum.network;

public class GuestState {
    private int joystickState;
    private int joystickType;

    public GuestState(final int joystickState, final int joystickType) {
        this.joystickState = joystickState;
        this.joystickType = joystickType;
    }

    public int getJoystickState() {
        return joystickState;
    }

    public int getJoystickType() {
        return joystickType;
    }
}
