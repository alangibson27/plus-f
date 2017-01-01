package com.socialthingy.plusf.spectrum.ui;

import akka.actor.ActorSystem;
import akka.japi.Option;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.spectrum.Settings;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestPeerAdapter;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import com.socialthingy.plusf.z80.Memory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Optional;
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
    private final GuestULA ula;
    private EmulatorState lastHostData;
    private final int[] memory;
    private int count = 0;
    private final ScheduledThreadPoolExecutor cycleScheduler;
    private JCheckBoxMenuItem portForwardingEnabled;

    public Guest() {
        memory = new int[0x10000];
        Memory.configure(memory, Model._48K);
        ula = new GuestULA();
        display = DisplayFactory.create();
        joystick = new SwingJoystick();

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        setTitle("+F Spectrum Guest");
        final ActorSystem actorSystem = ActorSystem.apply("GuestActorSystem", Settings.config);
        peer = new GuestPeerAdapter(actorSystem, hostData -> lastHostData = hostData);

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
        portForwardingEnabled = new JCheckBoxMenuItem("Use port forwarding");
        portForwardingEnabled.setSelected(true);
        networkMenu.add(portForwardingEnabled);
        menuBar.add(networkMenu);

        connectItem.setEnabled(true);
        disconnectItem.setEnabled(false);
        peer.connectedProperty().addObserver((observable, arg) -> {
            connectItem.setEnabled(!peer.connectedProperty().get());
            disconnectItem.setEnabled(peer.connectedProperty().get());
        });

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
        final String codename = JOptionPane.showInputDialog(
            this,
            "Enter codename of the Emulator",
            "Connect to Emulator",
            JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            final Option<Object> port;
            if (portForwardingEnabled.isSelected()) {
                port = Option.some(Settings.GUEST_PORT);
            } else {
                port = Option.none();
            }
            peer.connect(this, codename, port.asScala());
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
    }

    private void refresh() {
        if (lastHostData != null) {
            System.arraycopy(lastHostData.getMemory(), 0x4000, memory, 0x4000, 0x1b00);
            ula.setBorderChanges(lastHostData.getBorderChanges());
            EventQueue.invokeLater(() -> {
                display.updateScreen(memory, ula);
                display.updateBorder(ula, true);
                display.repaint();
            });
        }

        if (count % 5 == 0 && peer.isConnected()) {
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

class GuestULA extends ULA {
    GuestULA() {
        super(null, null, null);
    }

    void setBorderChanges(final List<Long> borderChanges) {
        getBorderChanges().clear();
        getBorderChanges().addAll(borderChanges);
    }

    @Override
    public boolean borderNeedsRedrawing() {
        return true;
    }
}