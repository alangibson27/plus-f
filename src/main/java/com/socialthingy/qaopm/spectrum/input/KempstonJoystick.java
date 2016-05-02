package com.socialthingy.qaopm.spectrum.input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KempstonJoystick implements EventHandler<KeyEvent> {
    public enum Button {
        FIRE(0b00010000),
        UP(0b00001000),
        DOWN(0b00000100),
        LEFT(0b00000010),
        RIGHT(0b00000001);

        private int mask;

        Button(int mask) {
            this.mask = mask;
        }
    }

    private Map<KeyCode, Button> buttonKeyAssignments = new HashMap<>();

    public KempstonJoystick() {
        buttonKeyAssignments.put(KeyCode.Q, Button.UP);
        buttonKeyAssignments.put(KeyCode.A, Button.DOWN);
        buttonKeyAssignments.put(KeyCode.O, Button.LEFT);
        buttonKeyAssignments.put(KeyCode.P, Button.RIGHT);
        buttonKeyAssignments.put(KeyCode.M, Button.FIRE);
    }

    private Set<Button> buttonsActive = new HashSet<>();

    @Override
    public void handle(final KeyEvent event) {
        final Button pressed = buttonKeyAssignments.get(event.getCode());
        if (pressed != null) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                buttonsActive.add(pressed);
            } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                buttonsActive.remove(pressed);
            }
        }
    }

    public int getPortValue() {
        return buttonsActive.stream().mapToInt(b -> b.mask).reduce(0, (a, b) -> a + b);
    }
}
