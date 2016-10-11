package com.socialthingy.plusf.spectrum;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public enum EmulatorSpeed {
    NORMAL("Normal", 20, TimeUnit.MILLISECONDS, KeyEvent.VK_F1),
    FAST("Fast", 15, TimeUnit.MILLISECONDS, KeyEvent.VK_F2),
    DOUBLE("Double", 10, TimeUnit.MILLISECONDS, KeyEvent.VK_F3),
    TURBO("Turbo", 0, TimeUnit.NANOSECONDS, KeyEvent.VK_F4);

    public final String displayName;
    public final int period;
    public final TimeUnit timeUnit;
    public final int shortcutKey;

    EmulatorSpeed(final String displayName, final int period, final TimeUnit timeUnit, final int shortcutKey) {
        this.displayName = displayName;
        this.period = period;
        this.timeUnit = timeUnit;
        this.shortcutKey = shortcutKey;
    }
}
