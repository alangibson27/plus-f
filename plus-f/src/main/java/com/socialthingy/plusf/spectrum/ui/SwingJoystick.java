package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.joystick.Control;
import com.socialthingy.plusf.spectrum.joystick.Joystick;
import com.socialthingy.plusf.ui.JoystickKeys;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class SwingJoystick extends Joystick implements KeyListener {
    private Map<Integer, Control> controlKeyAssignments = new HashMap<>();
    private JoystickKeys keys;

    public SwingJoystick(final JoystickKeys keys) {
        setKeys(keys);
    }

    public void setKeys(final JoystickKeys keys) {
        this.keys = keys;
        controlKeyAssignments.clear();
        controlKeyAssignments.put(keys.getUp(), Control.UP);
        controlKeyAssignments.put(keys.getDown(), Control.DOWN);
        controlKeyAssignments.put(keys.getLeft(), Control.LEFT);
        controlKeyAssignments.put(keys.getRight(), Control.RIGHT);
        controlKeyAssignments.put(keys.getFire(), Control.FIRE);
    }

    public JoystickKeys getKeys() {
        return keys;
    }

    @Override
    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        final Control pressed = controlKeyAssignments.get(e.getKeyCode());
        if (pressed != null) {
            e.consume();
            controlActive(pressed);
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        final Control released = controlKeyAssignments.get(e.getKeyCode());
        if (released != null) {
            e.consume();
            controlInactive(released);
        }
    }
}
