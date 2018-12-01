package com.socialthingy.plusf.spectrum.ui;

import com.codahale.metrics.MetricRegistry;
import com.socialthingy.plusf.snapshot.Snapshot;
import com.socialthingy.plusf.sound.SoundSystem;
import com.socialthingy.plusf.spectrum.*;
import com.socialthingy.plusf.spectrum.display.DisplayComponent;
import com.socialthingy.plusf.spectrum.input.HostInputMultiplexer;
import com.socialthingy.plusf.spectrum.io.*;
import com.socialthingy.plusf.spectrum.joystick.*;
import com.socialthingy.plusf.spectrum.network.EmulatorPeerAdapter;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.wos.Archive;
import com.socialthingy.plusf.wos.WosTree;
import com.socialthingy.plusf.wos.ZipUtils;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static com.socialthingy.plusf.spectrum.UserPreferences.*;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.joystickItem;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Optional.empty;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class Emulator extends PlusFComponent implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Emulator.class);

    protected Computer computer;
    private final Frame window;
    private final Runnable switchListener;
    private final DisplayComponent display;
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
    private long lastRepaint;
    private final Joystick guestJoystick;
    private final KempstonJoystickInterface kempstonJoystickInterface;
    private final SinclairJoystickInterface sinclair1JoystickInterface;
    private final SwingJoystick joystick;
    private final SoundSystem soundSystem = new SoundSystem();
    private final Clock clock = new Clock();
    private boolean turboLoadActive;
    private boolean turboLoadEnabled;
    private JMenuItem kempstonJoystickItem;
    private JMenuItem sinclairJoystickItem;
    private JMenuItem noJoystickItem;

    public Emulator(final Frame window, final Runnable switchListener) {
        this(window, switchListener, new UserPreferences(), new DisplayComponent());
    }

    protected Emulator(final Frame window, final Runnable switchListener, final UserPreferences prefs, final DisplayComponent suppliedDisplay) {
        this.switchListener = switchListener;
        this.window = window;
        this.prefs = prefs;
        this.display = suppliedDisplay;

        currentModel = Model.valueOf(prefs.getOrElse(MODEL, Model._48K.name()));
        keyboard = new SwingKeyboard();
        tapePlayer = new TapePlayer();

        joystick = new SwingJoystick();
        kempstonJoystickInterface = new KempstonJoystickInterface();
        sinclair1JoystickInterface = new SinclairJoystickInterface(keyboard);
        guestJoystick = new Joystick();
        hostInputMultiplexer = new HostInputMultiplexer(keyboard, joystick);
        hostInputMultiplexer.deactivateJoystick();
        kempstonJoystickInterface.connect(guestJoystick);

        peer = new EmulatorPeerAdapter(this::receiveGuestState);

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        speedIndicator = new JLabel("Normal speed");
        computer = newComputer(currentModel);

        initialiseUI();
    }

    private void receiveGuestState(final GuestState guestState) {
        guestJoystick.deserialise(guestState.getJoystickState());
        final JoystickInterfaceType jt = JoystickInterfaceType.from(guestState.getJoystickType());
        switch (jt) {
            case KEMPSTON:
                kempstonJoystickItem.setEnabled(false);
                sinclairJoystickItem.setEnabled(true);
                disconnectJoystickIfConnected(joystick, kempstonJoystickInterface);
                sinclair1JoystickInterface.disconnectIfConnected(guestJoystick);
                kempstonJoystickInterface.connect(guestJoystick);
                break;

            case SINCLAIR_1:
                kempstonJoystickItem.setEnabled(true);
                sinclairJoystickItem.setEnabled(false);
                disconnectJoystickIfConnected(joystick, sinclair1JoystickInterface);
                kempstonJoystickInterface.disconnectIfConnected(guestJoystick);
                sinclair1JoystickInterface.connect(guestJoystick);
                break;
        }
    }

    private void disconnectJoystickIfConnected(
        final Joystick joystick,
        final JoystickInterface joystickInterface
    ) {
        if (joystickInterface.disconnectIfConnected(joystick)) {
            noJoystickItem.setSelected(true);
            hostInputMultiplexer.deactivateJoystick();
            whilePaused(() -> {
                JOptionPane.showMessageDialog(
                        window,
                        format("The other player has taken control of the %s joystick. Switching to keyboard.", joystickInterface.getType().displayName),
                        "joystick Changed",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });
        }
    }

    private Computer newComputer(final Model model) {
        return newComputer(model, null);
    }

    private Computer newComputer(final Model model, final Snapshot snapshot) {
        final SpectrumMemory memory;
        final ContentionModel contentionModel;
        if (snapshot == null) {
            switch (model) {
                case _128K:
                case _128K_SPANISH:
                case PLUS_2:
                    memory = new Memory128K(model);
                    contentionModel = new ContentionModel128K(clock, model, memory);
                    break;

                case PLUS_2A:
                    memory = new MemoryPlus2A();
                    contentionModel = new ContentionModelPlus2A(clock, memory);
                    break;

                case _48K:
                default:
                    memory = new Memory48K();
                    contentionModel = new ContentionModel48K(clock, model, memory);
                    break;
            }
        } else {
            memory = snapshot.getMemory();
            contentionModel = snapshot.getContentionModel(clock, memory);
        }

        final ULA ula = new ULA(memory, keyboard, tapePlayer, soundSystem.getBeeper(), clock, model);
        final IOMultiplexer ioMux = new IOMultiplexer(memory, ula, soundSystem.getAyChip(), kempstonJoystickInterface);

        final Processor processor = new Processor(memory, contentionModel, ioMux, clock);
        if (snapshot != null) {
            snapshot.setProcessorState(processor);
            snapshot.setBorderColour(ula);
        }

        soundSystem.getBeeper().setModel(model);
        return new Computer(
            processor,
            memory,
            ula,
            new MetricRegistry()
        );
    }

    private void initialiseUI() {
        final TapeControls tapeControls = new TapeControls(tapePlayer);
        final JPanel statusBar = new JPanel(new GridLayout(1, 4));
        speedIndicator.setHorizontalAlignment(SwingConstants.TRAILING);
        statusBar.add(
            new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );
        statusBar.add(tapeControls);
        statusBar.add(speedIndicator);

        final Insets insets = new Insets(1, 1, 1, 1);
        setLayout(new GridBagLayout());
        add(
            display,
            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, insets, 0, 0)
        );
        add(
            statusBar,
            new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0)
        );
    }

    private void addJoystickMenu(final JMenu computerMenu) {
        final ButtonGroup JoystickButtonGroup = new ButtonGroup();

        final JMenu joystickMenu = new JMenu("Joystick");
        computerMenu.add(joystickMenu);

        noJoystickItem = joystickItem(JoystickButtonGroup, "None", true);
        joystickMenu.add(noJoystickItem);
        kempstonJoystickItem = joystickItem(JoystickButtonGroup, "Kempston", false);
        joystickMenu.add(kempstonJoystickItem);
        sinclairJoystickItem = joystickItem(JoystickButtonGroup, "Sinclair", false);
        joystickMenu.add(sinclairJoystickItem);

        noJoystickItem.addActionListener(event -> {
            hostInputMultiplexer.deactivateJoystick();
            kempstonJoystickInterface.disconnectIfConnected(joystick);
            sinclair1JoystickInterface.disconnectIfConnected(joystick);
        });

        kempstonJoystickItem.addActionListener(event -> {
            hostInputMultiplexer.activateJoystick();
            sinclair1JoystickInterface.disconnectIfConnected(joystick);
            kempstonJoystickInterface.connect(joystick);
        });

        sinclairJoystickItem.addActionListener(event -> {
            hostInputMultiplexer.activateJoystick();
            kempstonJoystickInterface.disconnectIfConnected(joystick);
            sinclair1JoystickInterface.connect(joystick);
        });
    }

    private void tapeInfo(final ActionEvent actionEvent) {
        final Tape tape = tapePlayer.getTape().get();
        final List<Pair<String, String>> info = tape.getArchiveInfo()
                .stream()
                .map(p -> new Pair<>(p.getFirst().replaceAll("\\s", " "), p.getSecond().replaceAll("\\s", " ")))
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
            info.stream().map(i -> new Object[] {i.getFirst(), i.getSecond()}).forEach(tableModel::addRow);

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
        final List<NavigableBlock> blocks = tape.getNavigableBlocks();

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
                    return super.getListCellRendererComponent(list, selected.getBlock().getDisplayName(), index, isSelected, cellHasFocus);
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
                    tapePlayer.jumpToBlock(blockList.getSelectedValue().getIndex());
                } catch (TapeException ex) {
                    log.error("Unable to jump to tape block", ex);
                }
            }
        }
    }

    public void run() {
        soundSystem.start();
        setMinimumSize(getSize());
        setSpeed(EmulatorSpeed.NORMAL);
    }

    public void stop() {
        if (cycleTimer != null) {
            cycleTimer.cancel(true);
        }
        peer.shutdown();
        soundSystem.stop();
    }

    private void connect(final ActionEvent e) {
        final JPanel connectDetailsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        connectDetailsPanel.add(new JLabel("Name of Session to start"));
        final JCheckBox portForwardingEnabled = new JCheckBox("I have enabled port forwarding", true);
        connectDetailsPanel.add(portForwardingEnabled);
        final String codename = JOptionPane.showInputDialog(
                this,
                connectDetailsPanel,
                "Start Session",
                JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            final Optional<Integer> port;
            if (portForwardingEnabled.isSelected()) {
                port = Optional.of(Settings.COMPUTER_PORT);
            } else {
                port = Optional.empty();
            }
            peer.connect(window, codename, port);
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
        kempstonJoystickItem.setEnabled(true);
        sinclairJoystickItem.setEnabled(true);
    }

    private void setSpeed(final EmulatorSpeed newSpeed) {
        speedIndicator.setText(format("%s speed", newSpeed.displayName));
        currentSpeed = newSpeed;
        if (cycleTimer != null) {
            cycleTimer.cancel(false);
        }

        if (newSpeed == EmulatorSpeed.TURBO) {
            cycleScheduler.execute(this::singleCycle);
        } else {
            turboLoadActive = false;
            cycleTimer = cycleScheduler.scheduleAtFixedRate(this::singleCycle, 0, newSpeed.period, newSpeed.timeUnit);
        }

        if (newSpeed != EmulatorSpeed.NORMAL) {
            soundSystem.setEnabled(false);
        }
    }

    private boolean sendToPeer = true;

    private void singleCycle() {
        try {
            do {
                computer.singleCycle();

                if (peer.isConnected() && currentSpeed == EmulatorSpeed.NORMAL && sendToPeer) {
                    final EmulatorState[] states = new EmulatorState[8];
                    final int[] displayMemory = computer.getDisplayMemory();
                    for (int i = 0; i < 8; i++) {
                        states[i] = new EmulatorState(displayMemory, i * 0x360, 0x360, computer.getBorderColours(), computer.flashActive());
                    }
                    peer.send(states);
                }
                sendToPeer = !sendToPeer;

                if (shouldRepaint()) {
                    soundSystem.getBeeper().play();
                    lastRepaint = System.currentTimeMillis();
                    display.updateScreen(computer.getScreenPixels());

                    if (computer.borderNeedsRedrawing() || currentSpeed == EmulatorSpeed.TURBO) {
                        display.updateBorder(computer.getBorderColours());
                    }
                    SwingUtilities.invokeLater(display::repaint);
                }

                handleTurboLoading();
            } while (currentSpeed == EmulatorSpeed.TURBO);
        } catch (Exception ex) {
            log.error("Unexpected error during execution cycle", ex);
        }
    }

    private void handleTurboLoading() {
        final boolean ulaAccessed = computer.ulaAccessed();
        if (!turboLoadActive && ulaAccessed && turboLoadEnabled
                && tapePlayer.isPlaying() && currentSpeed != EmulatorSpeed.TURBO) {
            turboLoadActive = true;
            setSpeed(EmulatorSpeed.TURBO);
        }

        if (turboLoadActive && (!ulaAccessed || !tapePlayer.isPlaying())) {
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
                        format("An error occurred while loading the file file:\n%s", ex.getMessage()),
                        "Loading Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    private void loadFromWos(final ActionEvent e) {
        whilePaused(() -> {
            final WosTree chooser = new WosTree(window);
            chooser.setSize(500, 600);
            chooser.setLocationRelativeTo(this);
            final Optional<Archive> result = chooser.selectArchive();
            result.ifPresent(this::loadFromArchive);
        });
    }

    private void loadFromArchive(final Archive archive) {
        try (final InputStream is = archive.getLocation().openStream()) {
            final int keepFile = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to save this archive?",
                    "Save Downloaded Archive?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            final File downloaded;
            if (keepFile == JOptionPane.YES_OPTION) {
                final JFileChooser chooser = new JFileChooser("Load .TAP, .TZX or .Z80 file");
                if (prefs.definedFor(LAST_LOAD_DIRECTORY)) {
                    chooser.setCurrentDirectory(new File(prefs.get(LAST_LOAD_DIRECTORY)));
                }
                chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), archive.getName()));

                final int result = chooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    downloaded = chooser.getSelectedFile();
                } else {
                    downloaded = File.createTempFile("plusf", "zip");
                    downloaded.deleteOnExit();
                }
            } else {
                downloaded = File.createTempFile("plusf", "zip");
                downloaded.deleteOnExit();
            }

            Files.copy(is, downloaded.toPath(), REPLACE_EXISTING);
            detectAndLoad(downloaded);
        } catch (TapeException|IOException e) {
            JOptionPane.showMessageDialog(
                this,
                "There was an error opening the archive.",
                "Archive Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
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
        } else if (ZipUtils.INSTANCE.isZipFile(selectedFile)) {
            loadFromZip(selectedFile);
        } else {
            try (final FileInputStream fis = new FileInputStream(selectedFile)) {
                final Snapshot snapshot = new Snapshot(fis);
                computer = newComputer(snapshot.getModel(), snapshot);
                resetComputer();
            }
        }
    }

    private void loadFromZip(final File zipFile) throws IOException, TapeException {
        final List<ZipEntry> filesInZip = ZipUtils.INSTANCE.findFiles(zipFile);
        if (filesInZip.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No tape files were found in this archive",
                "No Tape Files Found",
                JOptionPane.ERROR_MESSAGE
            );
        } else if (filesInZip.size() == 1) {
            unzipAndLoad(zipFile, filesInZip.get(0));
        } else {
            final Optional<ZipEntry> fileInZip = selectFromZip(filesInZip);
            if (fileInZip.isPresent()) {
                unzipAndLoad(zipFile, fileInZip.get());
            }
        }
    }

    private void unzipAndLoad(final File selectedFile, final ZipEntry fileInZip) throws IOException, TapeException {
        final Optional<File> unzipped = ZipUtils.INSTANCE.unzipFile(selectedFile, fileInZip);
        if (unzipped.isPresent()) {
            detectAndLoad(unzipped.get());
        } else {
            JOptionPane.showMessageDialog(
                this,
                "There was a problem attempting to unzip the file from the archive.",
                "Archive Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Optional<ZipEntry> selectFromZip(final List<ZipEntry> filesInZip) {
        final DefaultListModel<ZipEntry> listModel = new DefaultListModel<>();
        filesInZip.forEach(listModel::addElement);
        final JList<ZipEntry> entryList = new JList<>(listModel);
        entryList.setSelectionMode(SINGLE_SELECTION);
        entryList.setSelectedIndex(0);

        final int result = JOptionPane.showConfirmDialog(
            this,
            entryList,
            "Select Tap",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return Optional.of(entryList.getSelectedValue());
        } else {
            return Optional.empty();
        }
    }

    private void changeModel(final Model newModel) {
        whilePaused(() -> {
            final int result = JOptionPane.showConfirmDialog(
                this,
                "This will reset the computer. Do you want to continue?",
                format("Change to %s", newModel.displayName),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                prefs.set(MODEL, newModel.name());
                currentModel = newModel;
                computer = newComputer(currentModel);
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
                computer = newComputer(currentModel);
                resetComputer();
            }
        });
    }

    protected void resetComputer() {
        keyboard.reset();
        soundSystem.reset();
        cycleTimer.cancel(false);
        turboLoadActive = false;
        setSpeed(EmulatorSpeed.NORMAL);
    }

    private void quit(final ActionEvent e) {
        System.exit(0);
    }

    private void whilePaused(final Runnable action) {
        final boolean wasEnabled = soundSystem.setEnabled(false);
        cycleTimer.cancel(true);
        currentSpeed = EmulatorSpeed.NORMAL;
        keyboard.reset();
        try {
            action.run();
        } finally {
            setSpeed(currentSpeed);
            soundSystem.setEnabled(wasEnabled);
        }
    }

    @Override
    JMenuBar getMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Load from file ...", this::load, Optional.of(KeyEvent.VK_L)));
        fileMenu.add(menuItemFor("Load from WOS ...", this::loadFromWos, Optional.of(KeyEvent.VK_W)));
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu computerMenu = new JMenu("Computer");
        computerMenu.add(menuItemFor("Reset", this::reset, Optional.of(KeyEvent.VK_R)));
        computerMenu.add(menuItemFor("Dump next cycle", e -> computer.startDumping(), Optional.of(KeyEvent.VK_Z)));

        final JCheckBoxMenuItem sound = new JCheckBoxMenuItem("Sound");
        sound.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        sound.addActionListener(e -> {
            prefs.set(SOUND_ENABLED, sound.isSelected());
            soundSystem.setEnabled(sound.isSelected());
        });
        sound.setSelected(prefs.getOrElse(SOUND_ENABLED, true));
        soundSystem.setEnabled(sound.isSelected());
        computerMenu.add(sound);

        addJoystickMenu(computerMenu);

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

        final JMenu displayMenu = new JMenu("Display");
        final JCheckBoxMenuItem smoothRendering = new JCheckBoxMenuItem("Smooth Display Rendering");
        smoothRendering.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        smoothRendering.addActionListener(e -> display.setSmoothRendering(smoothRendering.isSelected()));
        smoothRendering.doClick();
        displayMenu.add(smoothRendering);

        final JCheckBoxMenuItem extendBorder = new JCheckBoxMenuItem("Extend Border");
        extendBorder.addActionListener(e -> display.setExtendBorder(extendBorder.isSelected()));
        displayMenu.add(extendBorder);
        menuBar.add(displayMenu);

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
        enableTurboLoad.addItemListener(e -> {
            prefs.set(TURBO_LOAD, enableTurboLoad.isSelected());
            turboLoadEnabled = enableTurboLoad.isSelected();
        });
        if (prefs.getOrElse(TURBO_LOAD, true)) {
            enableTurboLoad.doClick();
        }
        tapeMenu.add(enableTurboLoad);

        menuBar.add(tapeMenu);

        final JMenu networkMenu = new JMenu("Network");
        final JMenuItem connectItem = menuItemFor("Start Session", this::connect, Optional.of(KeyEvent.VK_C));
        networkMenu.add(connectItem);
        final JMenuItem joinItem = menuItemFor("Join Session", e -> switchListener.run(), Optional.of(KeyEvent.VK_J));
        networkMenu.add(joinItem);
        final JMenuItem disconnectItem = menuItemFor("End Session", this::disconnect, Optional.of(KeyEvent.VK_D));
        networkMenu.add(disconnectItem);
        menuBar.add(networkMenu);

        connectItem.setEnabled(true);
        disconnectItem.setEnabled(false);
        peer.connectedProperty().addObserver((observable, arg) -> {
            connectItem.setEnabled(!peer.connectedProperty().get());
            joinItem.setEnabled(!peer.connectedProperty().get());
            disconnectItem.setEnabled(peer.connectedProperty().get());
        });

        final JMenu aboutMenu = new JMenu("About");
        final JMenuItem aboutItem = menuItemFor("About Plus-F", e -> AboutDialog.aboutDialog(this), Optional.empty());
        aboutMenu.add(aboutItem);
        menuBar.add(aboutMenu);

        return menuBar;
    }

    @Override
    KeyListener getKeyListener() {
        return hostInputMultiplexer;
    }
}
