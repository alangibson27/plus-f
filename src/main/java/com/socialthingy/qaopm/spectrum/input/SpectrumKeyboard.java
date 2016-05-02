package com.socialthingy.qaopm.spectrum.input;

import com.socialthingy.qaopm.spectrum.io.ULA;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.*;

import static javafx.scene.input.KeyCode.*;

public class SpectrumKeyboard implements EventHandler<KeyEvent> {
    private final Map<KeyCode, Character> spectrumKeys = new HashMap<>();
    private final Map<KeyCode, List<KeyCode>> convenienceKeys = new HashMap<>();
    private final ULA ula;

    public SpectrumKeyboard(final ULA ula) {
        this.ula = ula;

        spectrumKeys.put(A, 'a');
        spectrumKeys.put(B, 'b');
        spectrumKeys.put(C, 'c');
        spectrumKeys.put(D, 'd');
        spectrumKeys.put(E, 'e');
        spectrumKeys.put(F, 'f');
        spectrumKeys.put(G, 'g');
        spectrumKeys.put(H, 'h');
        spectrumKeys.put(I, 'i');
        spectrumKeys.put(J, 'j');
        spectrumKeys.put(K, 'k');
        spectrumKeys.put(L, 'l');
        spectrumKeys.put(M, 'm');
        spectrumKeys.put(N, 'n');
        spectrumKeys.put(O, 'o');
        spectrumKeys.put(P, 'p');
        spectrumKeys.put(Q, 'q');
        spectrumKeys.put(R, 'r');
        spectrumKeys.put(S, 's');
        spectrumKeys.put(T, 't');
        spectrumKeys.put(U, 'u');
        spectrumKeys.put(V, 'v');
        spectrumKeys.put(W, 'w');
        spectrumKeys.put(X, 'x');
        spectrumKeys.put(Y, 'y');
        spectrumKeys.put(Z, 'z');

        spectrumKeys.put(DIGIT0, '0');
        spectrumKeys.put(DIGIT1, '1');
        spectrumKeys.put(DIGIT2, '2');
        spectrumKeys.put(DIGIT3, '3');
        spectrumKeys.put(DIGIT4, '4');
        spectrumKeys.put(DIGIT5, '5');
        spectrumKeys.put(DIGIT6, '6');
        spectrumKeys.put(DIGIT7, '7');
        spectrumKeys.put(DIGIT8, '8');
        spectrumKeys.put(DIGIT9, '9');

        spectrumKeys.put(SHIFT, '^');
        spectrumKeys.put(CONTROL, '$');
        spectrumKeys.put(SPACE, ' ');
        spectrumKeys.put(ENTER, '_');

        addConvenienceKey(BACK_SPACE, SHIFT, DIGIT0);
        addConvenienceKey(COMMA, CONTROL, N);
        addConvenienceKey(PERIOD, CONTROL, M);
        addConvenienceKey(UP, SHIFT, DIGIT7);
        addConvenienceKey(DOWN, SHIFT, DIGIT6);
        addConvenienceKey(LEFT, SHIFT, DIGIT5);
        addConvenienceKey(RIGHT, SHIFT, DIGIT8);
        addConvenienceKey(COLON, CONTROL, Z);
        addConvenienceKey(SLASH, CONTROL, V);
        addConvenienceKey(MINUS, CONTROL, J);
        addConvenienceKey(PLUS, CONTROL, K);
        addConvenienceKey(EQUALS, CONTROL, L);
        addConvenienceKey(SEMICOLON, CONTROL, O);
        addConvenienceKey(AT, CONTROL, DIGIT2);
        addConvenienceKey(POUND, CONTROL, DIGIT3);
        addConvenienceKey(QUOTE, CONTROL, DIGIT7);
        addConvenienceKey(UNDERSCORE, CONTROL, DIGIT0);
    }

    @Override
    public void handle(final KeyEvent event) {
        final KeyCode keyCode = event.getCode();
        final EventType<KeyEvent> eventType = event.getEventType();

        if (handleSpectrumKey(keyCode, eventType)) {
            return;
        }

        if (convenienceKeys.containsKey(keyCode)) {
            convenienceKeys.get(keyCode)
                    .forEach(sk -> handleSpectrumKey(sk, eventType));
        }
    }

    private boolean handleSpectrumKey(final KeyCode keyCode, final EventType<KeyEvent> eventType) {
        final Character spectrumKey = spectrumKeys.get(keyCode);
        if (spectrumKey != null) {
            if (eventType == KeyEvent.KEY_PRESSED) {
                ula.keyDown(spectrumKey);
            } else if (eventType == KeyEvent.KEY_RELEASED) {
                ula.keyUp(spectrumKey);
            }
        }

        return spectrumKey != null;
    }

    private void addConvenienceKey(final KeyCode convenienceKey, final KeyCode ... spectrumKeys) {
        convenienceKeys.put(convenienceKey, Arrays.asList(spectrumKeys));
    }
}
