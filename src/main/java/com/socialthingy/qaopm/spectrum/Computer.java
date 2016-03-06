package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.snapshot.SnapshotLoader;
import com.socialthingy.qaopm.z80.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Computer extends Application implements InterruptingDevice {

    private final Processor processor;
    private final IO io;
    private final int[] memory;
    private final Display display;
    public static final int T_STATES_PER_REFRESH = 80000;
    private ImageView imageView;
    private PrintWriter dumpFile;

    public static void main(final String[] args) throws IOException {
        Application.launch(args);
    }

    public Computer() throws IOException {
        memory = new int[0x10000];
        io = new DummyIO();
        processor = new Processor(memory, io);
        display = new Display(memory);
        dumpFile = new PrintWriter(new FileWriter("/var/tmp/java_spectrum.dump"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String romFile = getParameters().getRaw().get(0);
        final String snapshotFile = getParameters().getRaw().get(1);

        try (final FileInputStream fis = new FileInputStream(romFile)) {
            int addr = 0;
            for (int next = fis.read(); next != -1; next = fis.read()) {
                memory[addr++] = next;
            }
        }

        loadSnapshot(snapshotFile);

        Group root = new Group();
        Scene scene = new Scene(root);

        imageView = new ImageView(display.getScreen());
        root.getChildren().add(imageView);
        primaryStage.setScene(scene);
        primaryStage.show();

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::singleRefresh, 0, 40, TimeUnit.MILLISECONDS);
    }

    private void loadSnapshot(String snapshotFile) throws IOException {
        try (final FileInputStream fis = new FileInputStream(snapshotFile)) {
            final SnapshotLoader sl = new SnapshotLoader(fis);
            sl.read(processor, memory);
        }
    }

    public void singleRefresh() {
        Platform.runLater(() -> imageView.setImage(display.refresh()));

        int tStates = 0;
        processor.interrupt(new InterruptRequest(this));

        while (tStates < T_STATES_PER_REFRESH) {
            try {
                processor.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            tStates += processor.lastTime();
        }
    }

    private void dumpState() {
        dumpFile.println(String.format("af: %04x bc: %04x de: %04x hl: %04x",
                processor.register("af").get(),
                processor.register("bc").get(),
                processor.register("de").get(),
                processor.register("hl").get()));
        dumpFile.println(String.format("af':%04x bc':%04x de':%04x hl':%04x",
                processor.register("af'").get(),
                processor.register("bc'").get(),
                processor.register("de'").get(),
                processor.register("hl'").get()));
        dumpFile.println(String.format("ix: %04x iy: %04x pc: %04x sp: %04x ir:%02x%02x",
                processor.register("ix").get(),
                processor.register("iy").get(),
                processor.register("pc").get(),
                processor.register("sp").get(),
                processor.register("i").get(),
                processor.register("r").get()));
        dumpFile.println(String.format("iff1: %s  iff2: %s  im: %d",
                processor.getIff(0) ? "True" : "False",
                processor.getIff(1) ? "True" : "False",
                processor.getInterruptMode()));
        dumpFile.println();
        dumpFile.flush();
    }

    @Override
    public void acknowledge() {
    }
}

class DummyIO implements IO {
    @Override
    public int read(int port, int accumulator) {
        return 191;
    }

    @Override
    public void write(int port, int accumulator, int value) {
    }
}