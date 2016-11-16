package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.io.Keyboard;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.event.KeyEvent.*;

public class SwingKeyboard extends Keyboard implements KeyListener {
    private Map<Integer, Character> spectrumKeys = new HashMap<>();
    private final Map<Integer, List<Integer>> convenienceKeys = new HashMap<>();

    public SwingKeyboard() {
        spectrumKeys.put(VK_A, 'a');
        spectrumKeys.put(VK_B, 'b');
        spectrumKeys.put(VK_C, 'c');
        spectrumKeys.put(VK_D, 'd');
        spectrumKeys.put(VK_E, 'e');
        spectrumKeys.put(VK_F, 'f');
        spectrumKeys.put(VK_G, 'g');
        spectrumKeys.put(VK_H, 'h');
        spectrumKeys.put(VK_I, 'i');
        spectrumKeys.put(VK_J, 'j');
        spectrumKeys.put(VK_K, 'k');
        spectrumKeys.put(VK_L, 'l');
        spectrumKeys.put(VK_M, 'm');
        spectrumKeys.put(VK_N, 'n');
        spectrumKeys.put(VK_O, 'o');
        spectrumKeys.put(VK_P, 'p');
        spectrumKeys.put(VK_Q, 'q');
        spectrumKeys.put(VK_R, 'r');
        spectrumKeys.put(VK_S, 's');
        spectrumKeys.put(VK_T, 't');
        spectrumKeys.put(VK_U, 'u');
        spectrumKeys.put(VK_V, 'v');
        spectrumKeys.put(VK_W, 'w');
        spectrumKeys.put(VK_X, 'x');
        spectrumKeys.put(VK_Y, 'y');
        spectrumKeys.put(VK_Z, 'z');

        spectrumKeys.put(VK_0, '0');
        spectrumKeys.put(VK_1, '1');
        spectrumKeys.put(VK_2, '2');
        spectrumKeys.put(VK_3, '3');
        spectrumKeys.put(VK_4, '4');
        spectrumKeys.put(VK_5, '5');
        spectrumKeys.put(VK_6, '6');
        spectrumKeys.put(VK_7, '7');
        spectrumKeys.put(VK_8, '8');
        spectrumKeys.put(VK_9, '9');

        spectrumKeys.put(VK_SHIFT, '^');
        spectrumKeys.put(VK_CONTROL, '$');
        spectrumKeys.put(VK_SPACE, ' ');
        spectrumKeys.put(VK_ENTER, '_');

        addConvenienceKey(VK_BACK_SPACE, VK_SHIFT, VK_0);
        addConvenienceKey(VK_COMMA, VK_CONTROL, VK_N);
        addConvenienceKey(VK_PERIOD, VK_CONTROL, VK_M);
        addConvenienceKey(VK_UP, VK_SHIFT, VK_7);
        addConvenienceKey(VK_DOWN, VK_SHIFT, VK_6);
        addConvenienceKey(VK_LEFT, VK_SHIFT, VK_5);
        addConvenienceKey(VK_RIGHT, VK_SHIFT, VK_8);
        addConvenienceKey(VK_COLON, VK_CONTROL, VK_Z);
        addConvenienceKey(VK_SLASH, VK_CONTROL, VK_V);
        addConvenienceKey(VK_MINUS, VK_CONTROL, VK_J);
        addConvenienceKey(VK_ADD, VK_CONTROL, VK_K);
        addConvenienceKey(VK_EQUALS, VK_CONTROL, VK_L);
        addConvenienceKey(VK_SEMICOLON, VK_CONTROL, VK_O);
        addConvenienceKey(VK_AT, VK_CONTROL, VK_2);
//        addConvenienceKey(, VK_CONTROL, VK_3);
        addConvenienceKey(VK_QUOTE, VK_CONTROL, VK_7);
        addConvenienceKey(VK_UNDERSCORE, VK_CONTROL, VK_0);
    }

    @Override
    public void keyTyped(final KeyEvent event) {
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        if (handleSpectrumKey(event.getKeyCode(), event.getModifiers(), true)) {
            return;
        }

        if (convenienceKeys.containsKey(event.getKeyCode())) {
            convenienceKeys.get(event.getKeyCode())
                    .forEach(sk -> handleSpectrumKey(sk, event.getModifiers(), true));
        }
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        if (handleSpectrumKey(event.getKeyCode(), event.getModifiers(), false)) {
            return;
        }

        if (convenienceKeys.containsKey(event.getKeyCode())) {
            convenienceKeys.get(event.getKeyCode())
                    .forEach(sk -> handleSpectrumKey(sk, event.getModifiers(), false));
        }
    }

    private boolean handleSpectrumKey(final int keyCode, final int modifiers, final boolean pressed) {
        if ((modifiers & ALT_MASK) != 0) {
            return false;
        }

        final Character spectrumKey = spectrumKeys.get(keyCode);
        if (spectrumKey != null) {
            if (pressed) {
                keyDown(spectrumKey);
            } else {
                keyUp(spectrumKey);
            }
        }

        return spectrumKey != null;
    }

    private void addConvenienceKey(final Integer convenienceKey, final Integer ... spectrumKeys) {
        convenienceKeys.put(convenienceKey, Arrays.asList(spectrumKeys));
    }
}
