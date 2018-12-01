package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.util.Arrays;

public class PlusF extends JFrame {
    private PlusFComponent component;

    private PlusF() {
        setTitle("+F Spectrum Emulator");
        setIconImage(Icons.windowIcon);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void go() {
        setComponent(new Emulator(this, new SwitchListener()));
        setVisible(true);
    }

    private void setComponent(final PlusFComponent component) {
        if (this.component != null) {
            Arrays.stream(getKeyListeners()).forEach(this::removeKeyListener);
            this.component.stop();
        }

        this.component = component;
        setContentPane(component);
        setJMenuBar(component.getMenuBar());
        addKeyListener(component.getKeyListener());
        component.run();
        pack();
    }

    public static void main(final String... args) {
        System.setProperty("swing.plaf.metal.controlFont", "Sans-Serif");
        System.setProperty("swing.plaf.metal.userFont", "Sans-Serif");

        final PlusF plusF = new PlusF();
        plusF.go();
    }

    private class SwitchListener implements Runnable {
        @Override
        public void run() {
            final int result = JOptionPane.showConfirmDialog(
                    PlusF.this,
                    "This will reset the state of the computer. Continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                if (component instanceof Emulator) {
                    setComponent(new Guest(PlusF.this, this));
                } else {
                    setComponent(new Emulator(PlusF.this, this));
                }
            }
        }
    }
}
