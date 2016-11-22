package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class PlusF extends JFrame {
    public PlusF() {
        final JButton hostButton = new JButton(new ImageIcon(getClass().getResource("/icons/host.png")));
        hostButton.addActionListener(l -> start(new Emulator()));
        hostButton.setFocusPainted(false);
        final JLabel hostLabel = new JLabel("<html><b>Host</b><br/>For 1-player games, or starting a 2-player session</html>");
        final JButton guestButton = new JButton(new ImageIcon(getClass().getResource("/icons/guest.png")));
        guestButton.addActionListener(l -> start(new Guest()));
        guestButton.setFocusPainted(false);
        final JLabel guestLabel = new JLabel("<html><b>Guest</b><br/>For joining in a 2-player game</html>");

        final Insets insets = new Insets(5, 5, 5, 5);
        final Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.add(
                new JLabel("<html><span style='font-size:32px'>Welcome to Plus-F!</span><html>"),
                new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0, CENTER, VERTICAL, insets, 0, 0)
        );

        contentPane.add(
                new JLabel("Select which application you wish to run"),
                new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0, CENTER, VERTICAL, insets, 0, 0)
        );

        contentPane.add(new JPanel(), new GridBagConstraints(0, 2, 1, 2, 1.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));
        contentPane.add(hostButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));
        contentPane.add(hostLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0));
        contentPane.add(guestButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, WEST, HORIZONTAL, insets, 0, 0));
        contentPane.add(guestLabel, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0));
        contentPane.add(new JPanel(), new GridBagConstraints(3, 2, 1, 2, 1.0, 0.0, EAST, HORIZONTAL, insets, 0, 0));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();

        final Rectangle bounds = getGraphicsConfiguration().getBounds();
        setLocation((bounds.width - getWidth()) / 2, (bounds.height - getHeight()) / 2);
        setTitle("Plus-F");
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
