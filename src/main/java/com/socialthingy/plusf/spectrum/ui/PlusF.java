package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class PlusF extends JFrame {
    public PlusF() {
        final JButton hostButton = new JButton("Host");
        hostButton.addActionListener(l -> start(new Emulator()));
        final JButton guestButton = new JButton("Guest");
        guestButton.addActionListener(l -> start(new Guest()));

        final Insets insets = new Insets(2, 2, 2, 2);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.add(
                new JLabel("Welcome to Plus-F. Please select which screen to run."),
                new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, CENTER, BOTH, insets, 0, 0)
        );

        contentPane.add(new JPanel(), new GridBagConstraints(0, 1, 1, 2, 1.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));
        contentPane.add(hostButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0));
        contentPane.add(guestButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0));
        contentPane.add(new JPanel(), new GridBagConstraints(2, 1, 1, 2, 1.0, 0.0, EAST, HORIZONTAL, insets, 0, 0));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        final Rectangle bounds = getGraphicsConfiguration().getBounds();
        setLocation((bounds.width - getWidth()) / 2, (bounds.height - getHeight()) / 2);
    }

    private void go() {
        setVisible(true);
    }

    private void start(final Runnable runnable) {
        EventQueue.invokeLater(() -> {
            setVisible(false);
            dispose();
            runnable.run();
        });
    }

    public static void main(final String ... args) {
        System.setProperty("swing.plaf.metal.controlFont", "Sans-Serif");
        System.setProperty("swing.plaf.metal.userFont", "Sans-Serif");
        final PlusF plusF = new PlusF();
        plusF.go();
    }
}
