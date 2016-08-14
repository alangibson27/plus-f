package com.socialthingy.plusf.spectrum.input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class KeyEventDelegate implements EventHandler<KeyEvent> {
    private final EventHandler<KeyEvent> keyboard;
    private final EventHandler<KeyEvent> kempstonJoystick;

    public KeyEventDelegate(
        final EventHandler<KeyEvent> keyboard,
        final EventHandler<KeyEvent> kempstonJoystick
    ) {
        this.keyboard = keyboard;
        this.kempstonJoystick = kempstonJoystick;
    }

    @Override
    public void handle(final KeyEvent event) {
        kempstonJoystick.handle(event);
        if (!event.isConsumed()) {
            keyboard.handle(event);
        }
    }
}
