package com.socialthingy.plusf.spectrum.input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class HostInputMultiplexer implements EventHandler<KeyEvent> {
    private final EventHandler<KeyEvent> keyboardHandler;
    private final EventHandler<KeyEvent> joystickHandler;
    private boolean joystickActive;

    public HostInputMultiplexer(
        final EventHandler<KeyEvent> keyboardHandler,
        final EventHandler<KeyEvent> joystickHandler
    ) {
        this.keyboardHandler = keyboardHandler;
        this.joystickHandler = joystickHandler;
    }

    public void activateJoystick() {
        this.joystickActive = true;
    }

    public void deactivateJoystick() {
        this.joystickActive = false;
    }

    @Override
    public void handle(final KeyEvent event) {
        if (joystickActive) {
            joystickHandler.handle(event);
        }

        if (!event.isConsumed()) {
            keyboardHandler.handle(event);
        }
    }
}
