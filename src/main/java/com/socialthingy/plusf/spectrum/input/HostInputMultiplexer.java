package com.socialthingy.plusf.spectrum.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class HostInputMultiplexer implements KeyListener {
    private final KeyListener keyboardHandler;
    private final KeyListener joystickHandler;
    private boolean joystickActive;

    public HostInputMultiplexer(
        final KeyListener keyboardHandler,
        final KeyListener joystickHandler
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
    public void keyTyped(final KeyEvent e) {
        if (joystickActive) {
            joystickHandler.keyTyped(e);
        }

        if (!e.isConsumed()) {
            keyboardHandler.keyTyped(e);
        }
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        if (joystickActive) {
            joystickHandler.keyPressed(e);
        }

        if (!e.isConsumed()) {
            keyboardHandler.keyPressed(e);
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        if (joystickActive) {
            joystickHandler.keyReleased(e);
        }

        if (!e.isConsumed()) {
            keyboardHandler.keyReleased(e);
        }
    }
}
