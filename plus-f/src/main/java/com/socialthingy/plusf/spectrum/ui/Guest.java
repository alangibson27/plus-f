package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.Settings;
import com.socialthingy.plusf.spectrum.display.DisplayComponent;
import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestPeerAdapter;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;

public class Guest extends JFrame implements Runnable {
    private final SwingJoystick joystick;
    private final GuestPeerAdapter peer;
    private final DisplayComponent display;
    private EmulatorState lastHostData;
    private int count = 0;
    private final ScheduledThreadPoolExecutor cycleScheduler;

    public Guest() {
        display = new DisplayComponent(new PixelMapper());
        joystick = new SwingJoystick();

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        setTitle("+F Spectrum Guest");
        peer = new GuestPeerAdapter(hostData -> lastHostData = hostData);

        initialiseUI();
    }

    private void initialiseUI() {
        setIconImage(Icons.windowIcon);

        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu displayMenu = new JMenu("Display");
        final JCheckBoxMenuItem smoothRendering = new JCheckBoxMenuItem("Smooth Display Rendering");
        smoothRendering.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        smoothRendering.addActionListener(e -> display.setSmoothRendering(smoothRendering.isSelected()));
        smoothRendering.doClick();
        displayMenu.add(smoothRendering);
        menuBar.add(displayMenu);

        final JMenu networkMenu = new JMenu("Network");
        final JMenuItem connectItem = menuItemFor("Connect", this::connect, Optional.of(KeyEvent.VK_C));
        networkMenu.add(connectItem);
        final JMenuItem disconnectItem = menuItemFor("Disconnect", this::disconnect, Optional.of(KeyEvent.VK_D));
        networkMenu.add(disconnectItem);
        menuBar.add(networkMenu);

        connectItem.setEnabled(true);
        disconnectItem.setEnabled(false);
        peer.connectedProperty().addObserver((observable, arg) -> {
            connectItem.setEnabled(!peer.connectedProperty().get());
            disconnectItem.setEnabled(peer.connectedProperty().get());
        });

        final JMenu aboutMenu = new JMenu("About");
        final JMenuItem aboutItem = menuItemFor("About Plus-F", e -> AboutDialog.aboutDialog(this), Optional.empty());
        aboutMenu.add(aboutItem);
        menuBar.add(aboutMenu);

        final JPanel statusBar = new JPanel(new GridLayout(1, 1));
        statusBar.add(
            new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );

        setJMenuBar(menuBar);
        addKeyListener(new JoystickHandler());

        final Insets insets = new Insets(1, 1, 1, 1);
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(
            display,
            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, insets, 0, 0)
        );
        getContentPane().add(
            statusBar,
            new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0)
        );
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void run() {
        setVisible(true);
        setMinimumSize(getSize());
        cycleScheduler.scheduleAtFixedRate(this::refresh, 0, 20, TimeUnit.MILLISECONDS);
    }

    private void connect(final ActionEvent e) {
        final JPanel connectDetailsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        connectDetailsPanel.add(new JLabel("Enter codename of the Emulator"));
        final JCheckBox portForwardingEnabled = new JCheckBox("I have enabled port forwarding", true);
        connectDetailsPanel.add(portForwardingEnabled);
        final String codename = JOptionPane.showInputDialog(
            this,
            connectDetailsPanel,
            "Connect to Emulator",
            JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            final Optional<Integer> port;
            if (portForwardingEnabled.isSelected()) {
                port = Optional.of(Settings.GUEST_PORT);
            } else {
                port = Optional.empty();
            }
            peer.connect(this, codename, port);
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
    }

    private void refresh() {
        if (lastHostData != null) {
            EventQueue.invokeLater(() -> {
                display.updateScreen(lastHostData.getMemory(), lastHostData.isFlashActive());
                display.updateBorder(lastHostData.getBorderColours());
                display.repaint();
            });
        }

        if (count % 20 == 0 && peer.isConnected()) {
            peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
        }

        count++;
    }

    private void quit(final ActionEvent e) {
        peer.shutdown();
        System.exit(0);
    }

    private class JoystickHandler implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            joystick.keyPressed(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            joystick.keyReleased(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
            }
        }
    }
}