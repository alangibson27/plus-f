package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.util.Arrays;

public class PlusF extends JFrame implements CloseListener {
    public static void main(final String... args) {
        System.setProperty("swing.plaf.metal.controlFont", "Sans-Serif");
        System.setProperty("swing.plaf.metal.userFont", "Sans-Serif");

        final PlusF plusF = new PlusF();
        plusF.go();
    }

    private PlusF() {
        setTitle("+F Spectrum Emulator");
        setIconImage(Icons.windowIcon);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void go() {
        setComponent(new Emulator(this));
        setVisible(true);
    }

    private void setComponent(final PlusFComponent component) {
        component.addCloseListener(this);
        setContentPane(component);
        setJMenuBar(component.getMenuBar());
        addKeyListener(component.getKeyListener());
        component.run();
        pack();
    }

    @Override
    public boolean willClose(final CloseEvent closeEvent) {
        final int result;
        if (closeEvent.isConfirmRequired()) {
            result = JOptionPane.showConfirmDialog(
                    PlusF.this,
                    "This will reset the state of the computer. Continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );
        } else {
            result = JOptionPane.OK_OPTION;
        }

        if (result == JOptionPane.OK_OPTION) {
            Arrays.stream(getKeyListeners()).forEach(this::removeKeyListener);
            closeEvent.getSource().stop();
            if (closeEvent.getSource() instanceof Emulator) {
                setComponent(new Guest(this));
            } else {
                setComponent(new Emulator(this));
            }

            return true;
        }

        return false;
    }
}
