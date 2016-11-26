package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.joystick.Control;
import com.socialthingy.plusf.spectrum.joystick.Joystick;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

public class SwingJoystick extends Joystick implements KeyListener {
    private Map<Integer, Control> controlKeyAssignments = new HashMap<>();

    public SwingJoystick() {
        controlKeyAssignments.put(KeyEvent.VK_Q, Control.UP);
        controlKeyAssignments.put(KeyEvent.VK_A, Control.DOWN);
        controlKeyAssignments.put(KeyEvent.VK_O, Control.LEFT);
        controlKeyAssignments.put(KeyEvent.VK_P, Control.RIGHT);
        controlKeyAssignments.put(KeyEvent.VK_M, Control.FIRE);
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
