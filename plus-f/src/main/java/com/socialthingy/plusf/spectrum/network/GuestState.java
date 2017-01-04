package com.socialthingy.plusf.spectrum.network;

public class GuestState {
    private int eventType;
    private int eventValue;

    public GuestState(final int eventType, final int eventValue) {
        this.eventType = eventType;
        this.eventValue = eventValue;
    }

    public int getEventType() {
        return eventType;
    }

    public int getEventValue() {
        return eventValue;
    }
}
