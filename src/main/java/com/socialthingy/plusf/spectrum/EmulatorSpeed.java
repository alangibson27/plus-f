package com.socialthingy.plusf.spectrum;

import javafx.scene.input.KeyCode;

import java.util.concurrent.TimeUnit;

public enum EmulatorSpeed {
    NORMAL("Normal", 20, TimeUnit.MILLISECONDS, KeyCode.F1),
    FAST("Fast", 15, TimeUnit.MILLISECONDS, KeyCode.F2),
    DOUBLE("Double", 10, TimeUnit.MILLISECONDS, KeyCode.F3),
    TURBO("Turbo", 0, TimeUnit.NANOSECONDS, KeyCode.F4);

    public final String displayName;
    public final int period;
    public final TimeUnit timeUnit;
    public final KeyCode shortcutKey;

    EmulatorSpeed(final String displayName, final int period, final TimeUnit timeUnit, final KeyCode shortcutKey) {
        this.displayName = displayName;
        this.period = period;
        this.timeUnit = timeUnit;
        this.shortcutKey = shortcutKey;
    }
}
