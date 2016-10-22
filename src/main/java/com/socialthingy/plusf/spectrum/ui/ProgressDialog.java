package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.awt.GridBagConstraints.*;

public class ProgressDialog extends JDialog {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;
    private final JLabel message = new JLabel("");

    public ProgressDialog(final Window parent, final String title, final Runnable onCancel) {
        super(parent, title);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new GridBagLayout());

        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        final JButton cancelButton = new JButton("Cancel");

        getContentPane().add(message, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(10, 10, 5, 10), 0, 0));
        getContentPane().add(progressBar, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, LINE_START, HORIZONTAL, new Insets(5, 10, 5, 10), 0, 0));
        getContentPane().add(cancelButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, LINE_END, NONE, new Insets(5, 5, 10, 10), 0, 0));

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setResizable(false);
        pack();

        cancelButton.addActionListener(ae -> {
            onCancel.run();
            close();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                onCancel.run();
                dispose();
            }
        });
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public void setMessage(final String messageText) {
        message.setText(messageText);
    }
}
