package com.socialthingy.plusf.spectrum.ui;

import com.codahale.metrics.MetricRegistry;
import com.socialthingy.plusf.spectrum.*;
import com.socialthingy.plusf.spectrum.input.HostInputMultiplexer;
import com.socialthingy.plusf.spectrum.io.IOMultiplexer;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.joystick.Joystick;
import com.socialthingy.plusf.spectrum.joystick.KempstonJoystickInterface;
import com.socialthingy.plusf.spectrum.joystick.SinclairJoystickInterface;
import com.socialthingy.plusf.spectrum.network.EmulatorPeerAdapter;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.socialthingy.plusf.spectrum.UserPreferences.LAST_LOAD_DIRECTORY;
import static com.socialthingy.plusf.spectrum.UserPreferences.MODEL;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.util.Optional.empty;

public class Emulator extends JFrame implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Emulator.class);

    private final Computer computer;
    private final DisplayComponent display;
    private final int[] memory;
    private final UserPreferences prefs;
    private final TapePlayer tapePlayer;
    private final HostInputMultiplexer hostInputMultiplexer;
    private final EmulatorPeerAdapter peer;
    private Model currentModel;
    private final SwingKeyboard keyboard;
    private final ScheduledThreadPoolExecutor cycleScheduler;
    private ScheduledFuture<?> cycleTimer;
    private EmulatorSpeed currentSpeed;
    private JLabel speedIndicator;
    private final ULA ula;
    private final Processor processor;
    private long lastRepaint;
    private final Joystick guestJoystick;
    private final KempstonJoystickInterface kempstonJoystickInterface;
    private final SinclairJoystickInterface sinclair1JoystickInterface;
    private final SwingJoystick hostJoystick;
    private boolean turboLoadActive;
    private boolean turboLoadEnabled;

    public Emulator() {
        this(new UserPreferences());
    }

    public Emulator(final UserPreferences prefs) {
        this(prefs, new int[0x10000], DisplayFactory.create());
    }

    protected Emulator(final UserPreferences prefs, final int[] suppliedMemory, final DisplayComponent suppliedDisplay) {
        if (suppliedMemory.length != 0x10000) {
            throw new IllegalArgumentException("Memory must be exactly 0x10000 in size.");
        }
        this.prefs = prefs;
        this.memory = suppliedMemory;
        this.display = suppliedDisplay;

        currentModel = Model.valueOf(prefs.getOrElse(MODEL, Model._48K.name()));
        Memory.configure(this.memory, currentModel);

        final IOMultiplexer ioMux = new IOMultiplexer();
        processor = new Processor(this.memory, ioMux);
        keyboard = new SwingKeyboard();
        tapePlayer = new TapePlayer();
        ula = new ULA(keyboard, tapePlayer, this.memory);

        hostJoystick = new SwingJoystick();
        kempstonJoystickInterface = new KempstonJoystickInterface();
        sinclair1JoystickInterface = new SinclairJoystickInterface(keyboard);
        guestJoystick = new Joystick();
        hostInputMultiplexer = new HostInputMultiplexer(keyboard, hostJoystick);
        hostInputMultiplexer.deactivateJoystick();
        kempstonJoystickInterface.connect(guestJoystick);

        ioMux.register(0xfe, ula);
        if (currentModel.ramPageCount > 1) {
            ioMux.register(0xfd, ula);
        }
        ioMux.register(0x1f, kempstonJoystickInterface);

        computer = new Computer(
            processor,
            ula,
            this.memory,
            Model._48K,
            new MetricRegistry()
        );

        peer = new EmulatorPeerAdapter(gs -> {
            if (gs.getEventType() == GuestStateType.JOYSTICK_STATE.ordinal()) {
                guestJoystick.deserialise(gs.getEventValue());
            }
        });

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        speedIndicator = new JLabel("Normal speed");

        setTitle("+F Spectrum Emulator");
        initialiseUI();
    }

    private void initialiseUI() {
        setIconImage(Icons.windowIcon);

        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Load", this::load, Optional.of(KeyEvent.VK_L)));
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu computerMenu = new JMenu("Computer");
        computerMenu.add(menuItemFor("Reset", this::reset, Optional.of(KeyEvent.VK_R)));

        final JCheckBoxMenuItem smoothRendering = new JCheckBoxMenuItem("Smooth Display Rendering");
        smoothRendering.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        smoothRendering.addActionListener(e -> display.setSmoothRendering(smoothRendering.isSelected()));
        smoothRendering.doClick();
        computerMenu.add(smoothRendering);

        addJoystickMenus(computerMenu);

        final JMenu modelMenu = new JMenu("Model");
        final ButtonGroup modelButtonGroup = new ButtonGroup();
        for (Model model: Model.values()) {
            final JRadioButtonMenuItem modelItem = new JRadioButtonMenuItem(
                model.displayName,
                model == currentModel
            );
            modelItem.addActionListener(item -> {
                if (currentModel != model) {
                    changeModel(model);
                }
            });
            modelMenu.add(modelItem);
            modelButtonGroup.add(modelItem);
        }
        computerMenu.add(modelMenu);

        final JMenu speedMenu = new JMenu("Speed");
        final ButtonGroup speedButtonGroup = new ButtonGroup();
        for (EmulatorSpeed speed: EmulatorSpeed.values()) {
            final JRadioButtonMenuItem speedItem = new JRadioButtonMenuItem(
                speed.displayName,
                speed == EmulatorSpeed.NORMAL
            );
            speedItem.addActionListener(item -> {
                if (currentSpeed != speed) {
                    setSpeed(speed);
                }
            });
            speedItem.setAccelerator(KeyStroke.getKeyStroke(speed.shortcutKey, 0));
            speedMenu.add(speedItem);
            speedButtonGroup.add(speedItem);
        }
        computerMenu.add(speedMenu);
        menuBar.add(computerMenu);

        final JMenu tapeMenu = new JMenu("Tape");
        final JCheckBoxMenuItem playTape = new JCheckBoxMenuItem("Play");
        playTape.setModel(tapePlayer.getPlayButtonModel());
        playTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        tapeMenu.add(playTape);

        final JMenuItem stopTape = new JMenuItem("Stop");
        stopTape.setModel(tapePlayer.getStopButtonModel());
        stopTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        tapeMenu.add(stopTape);

        final JMenuItem rewindTape = new JMenuItem("Rewind to Start");
        rewindTape.setModel(tapePlayer.getRewindToStartButtonModel());
        rewindTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        tapeMenu.add(rewindTape);

        final JMenuItem tapeInfo = menuItemFor("Tape Information ...", this::tapeInfo, empty());
        tapeInfo.setModel(tapePlayer.getTapePresentModel());
        tapeMenu.add(tapeInfo);

        final JMenuItem jumpToBlock = menuItemFor("Jump to Block ...", this::jumpToTapeBlock, empty());
        jumpToBlock.setModel(tapePlayer.getJumpButtonModel());
        tapeMenu.add(jumpToBlock);

        final JCheckBoxMenuItem enableTurboLoad = new JCheckBoxMenuItem("Turbo-load");
        enableTurboLoad.addItemListener(e -> turboLoadEnabled = enableTurboLoad.isSelected());
        enableTurboLoad.setSelected(true);
        tapeMenu.add(enableTurboLoad);

        menuBar.add(tapeMenu);

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

        final TapeControls tapeControls = new TapeControls(tapePlayer);
        final JPanel statusBar = new JPanel(new GridLayout(1, 3));
        speedIndicator.setHorizontalAlignment(SwingConstants.TRAILING);
        statusBar.add(
            new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );
        statusBar.add(tapeControls);
        statusBar.add(speedIndicator);

        setJMenuBar(menuBar);
        addKeyListener(hostInputMultiplexer);
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

    private JMenuItem joystickItem(final boolean host, final ButtonGroup group, final String type, final boolean selected) {
        final JMenuItem item = new JRadioButtonMenuItem(type, selected);
        item.setName((host ? "Host" : "Guest") + "Joystick" + type);
        group.add(item);
        return item;
    }

    private void addJoystickMenus(final JMenu computerMenu) {
        final ButtonGroup hostJoystickButtonGroup = new ButtonGroup();
        final ButtonGroup guestJoystickButtonGroup = new ButtonGroup();

        final JMenu hostJoystickMenu = new JMenu("Host Joystick");
        computerMenu.add(hostJoystickMenu);

        final JMenuItem noHostJoystick = joystickItem(true, hostJoystickButtonGroup, "None", true);
        hostJoystickMenu.add(noHostJoystick);
        final JMenuItem kempstonHostJoystick = joystickItem(true, hostJoystickButtonGroup, "Kempston", false);
        hostJoystickMenu.add(kempstonHostJoystick);
        final JMenuItem sinclairHostJoystick = joystickItem(true, hostJoystickButtonGroup, "Sinclair", false);
        hostJoystickMenu.add(sinclairHostJoystick);

        final JMenu guestJoystickMenu = new JMenu("Guest Joystick");
        computerMenu.add(guestJoystickMenu);

        final JMenuItem kempstonGuestJoystick = joystickItem(false, guestJoystickButtonGroup, "Kempston", true);
        guestJoystickMenu.add(kempstonGuestJoystick);
        final JMenuItem sinclairGuestJoystick = joystickItem(false, guestJoystickButtonGroup, "Sinclair", true);
        guestJoystickMenu.add(sinclairGuestJoystick);

        noHostJoystick.addActionListener(event -> {
            hostInputMultiplexer.deactivateJoystick();
            kempstonJoystickInterface.disconnectIfConnected(hostJoystick);
            sinclair1JoystickInterface.disconnectIfConnected(hostJoystick);
        });

        kempstonHostJoystick.addActionListener(event -> {
            hostInputMultiplexer.activateJoystick();
            sinclair1JoystickInterface.disconnectIfConnected(hostJoystick);
            if (kempstonJoystickInterface.isConnected(guestJoystick)) {
                guestJoystickButtonGroup.setSelected(sinclairGuestJoystick.getModel(), true);
                sinclair1JoystickInterface.connect(guestJoystick);
            }
            kempstonJoystickInterface.connect(hostJoystick);
        });

        sinclairHostJoystick.addActionListener(event -> {
            hostInputMultiplexer.activateJoystick();
            kempstonJoystickInterface.disconnectIfConnected(hostJoystick);
            if (sinclair1JoystickInterface.isConnected(guestJoystick)) {
                guestJoystickButtonGroup.setSelected(kempstonGuestJoystick.getModel(), true);
                kempstonJoystickInterface.connect(guestJoystick);
            }
            sinclair1JoystickInterface.connect(hostJoystick);
        });


        kempstonGuestJoystick.addActionListener(event -> {
            sinclair1JoystickInterface.disconnectIfConnected(guestJoystick);
            if (kempstonJoystickInterface.isConnected(hostJoystick)) {
                hostJoystickButtonGroup.setSelected(sinclairHostJoystick.getModel(), true);
                sinclair1JoystickInterface.connect(hostJoystick);
            }
            kempstonJoystickInterface.connect(guestJoystick);
        });

        sinclairGuestJoystick.addActionListener(event -> {
            kempstonJoystickInterface.disconnectIfConnected(guestJoystick);
            if (sinclair1JoystickInterface.isConnected(hostJoystick)) {
                hostJoystickButtonGroup.setSelected(kempstonHostJoystick.getModel(), true);
                kempstonJoystickInterface.connect(hostJoystick);
            }
            sinclair1JoystickInterface.connect(guestJoystick);
        });
    }

    private void tapeInfo(final ActionEvent actionEvent) {
        final Tape tape = tapePlayer.getTape().get();
        final List<Pair<String, String>> info = tape.archiveInfo()
                .stream()
                .map(p -> new Pair<>(p.first().replaceAll("\\s", " "), p.second().replaceAll("\\s", " ")))
                .collect(Collectors.toList());

        if (info.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No tape information available.",
                "Tape Information",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            final DefaultTableModel tableModel = new DefaultTableModel(0, 2);
            tableModel.setColumnIdentifiers(new String[] {"Name", "Value"});
            info.stream().map(i -> new Object[] {i.first(), i.second()}).forEach(tableModel::addRow);

            final JTable infoTable = new JTable(tableModel);
            infoTable.getColumnModel().getColumn(0).setMaxWidth(128);
            JOptionPane.showMessageDialog(
                this,
                new JScrollPane(infoTable),
                "Tape Information",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void jumpToTapeBlock(final ActionEvent actionEvent) {
        final Tape tape = tapePlayer.getTape().get();
        final List<NavigableBlock> blocks = tape.navigableBlocks();

        if (blocks.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tape is empty.",
                    "Jump to Block",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            final DefaultListModel<NavigableBlock> listModel = new DefaultListModel<>();
            blocks.forEach(listModel::addElement);

            final JList<NavigableBlock> blockList = new JList<>(listModel);
            blockList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                    final JList<?> list,
                    final Object value,
                    final int index,
                    final boolean isSelected,
                    final boolean cellHasFocus
                ) {
                    final NavigableBlock selected = (NavigableBlock) list.getModel().getElementAt(index);
                    return super.getListCellRendererComponent(list, selected.block().getDisplayName(), index, isSelected, cellHasFocus);
                }
            });
            final int result = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(blockList),
                    "Jump to Block",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION && blockList.getSelectedValue() != null) {
                try {
                    tapePlayer.jumpToBlock(blockList.getSelectedValue().index());
                } catch (TapeException ex) {
                    log.error("Unable to jump to tape block", ex);
                }
            }
        }
    }

    public void run() {
        setVisible(true);
        setMinimumSize(getSize());
        setSpeed(EmulatorSpeed.NORMAL);
    }

    public void stop() {
        if (cycleTimer != null) {
            cycleTimer.cancel(true);
        }
        peer.shutdown();
        setVisible(false);
        dispose();
    }

    private void connect(final ActionEvent e) {
        final JPanel connectDetailsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        connectDetailsPanel.add(new JLabel("Enter codename of the Guest"));
        final JCheckBox portForwardingEnabled = new JCheckBox("I have enabled port forwarding", true);
        connectDetailsPanel.add(portForwardingEnabled);
        final String codename = JOptionPane.showInputDialog(
                this,
                connectDetailsPanel,
                "Connect to Guest",
                JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            final Option<Object> port;
            if (portForwardingEnabled.isSelected()) {
                port = Option.apply(Settings.COMPUTER_PORT);
            } else {
                port = Option.empty();
            }
            peer.connect(this, codename, port);
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
    }

    private void setSpeed(final EmulatorSpeed newSpeed) {
        if (newSpeed != EmulatorSpeed.TURBO) {
            turboLoadActive = false;
        }

        speedIndicator.setText(String.format("%s speed", newSpeed.displayName));
        currentSpeed = newSpeed;
        if (cycleTimer != null) {
            cycleTimer.cancel(false);
        }
        if (newSpeed == EmulatorSpeed.TURBO) {
            cycleScheduler.execute(this::singleCycle);
        } else {
            cycleTimer = cycleScheduler.scheduleAtFixedRate(this::singleCycle, 0, newSpeed.period, newSpeed.timeUnit);
        }
    }

    private boolean sendToPeer = true;

    private void singleCycle() {
        try {
            do {
                computer.singleCycle();
                if (peer.isConnected() && currentSpeed == EmulatorSpeed.NORMAL && sendToPeer) {
                    final int[] screenBytes = Memory.getScreenBytes(memory);
                    peer.send(new EmulatorState(screenBytes, ula.getBorderChanges(), ula.flashActive()));
                }
                sendToPeer = !sendToPeer;

                if (shouldRepaint()) {
                    lastRepaint = System.currentTimeMillis();
                    display.updateScreen(memory, ula);
                    display.updateBorder(ula, currentSpeed == EmulatorSpeed.TURBO);
                    SwingUtilities.invokeLater(display::repaint);
                }

                handleTurboLoading();
            } while (currentSpeed == EmulatorSpeed.TURBO);
        } catch (Exception ex) {
            log.error("Unexpected error during execution cycle", ex);
        }
    }

    private void handleTurboLoading() {
        if (!turboLoadActive && ula.inFeExecuted() && turboLoadEnabled
                && tapePlayer.isPlaying() && currentSpeed != EmulatorSpeed.TURBO) {
            turboLoadActive = true;
            setSpeed(EmulatorSpeed.TURBO);
        }

        if (turboLoadActive && (!ula.inFeExecuted() || !tapePlayer.isPlaying())) {
            turboLoadActive = false;
            setSpeed(EmulatorSpeed.NORMAL);
        }
    }

    private boolean shouldRepaint() {
        return currentSpeed != EmulatorSpeed.TURBO || System.currentTimeMillis() - lastRepaint > 20;
    }

    private void load(final ActionEvent e) {
        whilePaused(() -> {
            final JFileChooser chooser = new JFileChooser("Load .TAP, .TZX or .Z80 file");
            if (prefs.definedFor(LAST_LOAD_DIRECTORY)) {
                chooser.setCurrentDirectory(new File(prefs.get(LAST_LOAD_DIRECTORY)));
            }

            final int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    detectAndLoad(chooser.getSelectedFile());
                    prefs.set(LAST_LOAD_DIRECTORY, chooser.getSelectedFile().getAbsolutePath());
                } catch (TapeException | IOException ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        String.format("An error occurred while loading the file file:\n%s", ex.getMessage()),
                        "Loading Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    private void detectAndLoad(final File selectedFile) throws IOException, TapeException {
        final byte[] prelude = new byte[256];
        try (final InputStream is = new FileInputStream(selectedFile)) {
            is.read(prelude);
        }

        if (selectedFile.getName().toLowerCase().endsWith(".tap")) {
            final Tape tap = new TapeFileReader(selectedFile).readTap();
            tapePlayer.setTape(tap);
        } else if (TapeFileReader.recognises(prelude)) {
            final Tape tzx = new TapeFileReader(selectedFile).readTzx();
            tapePlayer.setTape(tzx);
        } else {
            final int borderColour = computer.loadSnapshot(selectedFile);
            ula.setBorderColour(borderColour);
        }
    }

    private void changeModel(final Model newModel) {
        whilePaused(() -> {
            final int result = JOptionPane.showConfirmDialog(
                this,
                "This will reset the computer. Do you want to continue?",
                String.format("Change to %s", newModel.displayName),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                prefs.set(MODEL, newModel.name());
                currentModel = newModel;
                resetComputer();
            }
        });
    }

    private void reset(final ActionEvent e) {
        whilePaused(() -> {
            final int result = JOptionPane.showConfirmDialog(
               this,
               "Do you want to reset the computer?",
               "Reset",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                resetComputer();
            }
        });
    }

    protected void resetComputer() {
        Memory.configure(memory, Model.valueOf(prefs.getOrElse(MODEL, Model._48K.name())));
        computer.reset();
        processor.reset();
        ula.reset();
        tapePlayer.ejectTape();
        keyboard.reset();
        cycleTimer.cancel(false);
        turboLoadActive = false;
        setSpeed(EmulatorSpeed.NORMAL);
    }

    private void quit(final ActionEvent e) {
        System.exit(0);
    }

    private void whilePaused(final Runnable action) {
        cycleTimer.cancel(true);
        currentSpeed = EmulatorSpeed.NORMAL;
        keyboard.reset();
        try {
            action.run();
        } finally {
            setSpeed(currentSpeed);
        }
    }
}
