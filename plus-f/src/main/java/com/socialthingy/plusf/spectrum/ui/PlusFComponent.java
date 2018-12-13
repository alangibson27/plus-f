package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

abstract class PlusFComponent extends JComponent implements Runnable {
    private final List<CloseListener> closeListeners = new ArrayList<>();
    public void addCloseListener(final CloseListener closeListener) {
        closeListeners.add(closeListener);
    }

    public void removeCloseListener(final CloseListener closeListener) {
        closeListeners.remove(closeListener);
    }

    public void notifyWillClose(final boolean confirmRequired) {
        final CloseEvent ce = new CloseEvent(confirmRequired, this);
        closeListeners.removeIf(closeListener -> closeListener.willClose(ce));
    }

    abstract void stop();
    abstract JMenuBar getMenuBar();
    abstract KeyListener getKeyListener();
}

interface CloseListener {
    boolean willClose(final CloseEvent closeEvent);
}

class CloseEvent {
    private final boolean confirmRequired;
    private final PlusFComponent source;

    public CloseEvent(final boolean confirmRequired, final PlusFComponent source) {
        this.confirmRequired = confirmRequired;
        this.source = source;
    }

    public boolean isConfirmRequired() {
        return confirmRequired;
    }

    public PlusFComponent getSource() {
        return source;
    }
}