package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.spectrum.remote.Guest;
import com.socialthingy.qaopm.spectrum.remote.HostData;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.Iterator;

public class JavaFXGuest extends Application {

    public static final int SCREEN_WIDTH = 256;
    public static final int BORDER = 16;
    public static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    public static final int DISPLAY_HEIGHT = ULA.SCREEN_HEIGHT + (BORDER * 2);

    private final JavaFXDisplay display;
    private final JavaFXBorder border;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXGuest() {
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Iterator<String> params = getParameters().getRaw().iterator();
        final Guest guest = new Guest(
            Integer.parseInt(params.next()),
            InetAddress.getByName(params.next()),
            Integer.parseInt(params.next()),
            this::update
        );

        final ImageView borderImage = new ImageView(border.getBorder());
        borderImage.setFitWidth(DISPLAY_WIDTH * 2);
        borderImage.setFitHeight(DISPLAY_HEIGHT * 2);

        final ImageView screenImage = new ImageView(display.getScreen());
        screenImage.setFitHeight(ULA.SCREEN_HEIGHT * 2);
        screenImage.setFitWidth(SCREEN_WIDTH * 2);

        final StackPane sp = new StackPane(borderImage, screenImage);

        BorderPane root = new BorderPane();
        root.setBottom(sp);

        Scene scene = new Scene(root);

        primaryStage.setTitle("QAOPM Spectrum Emulator - GUEST");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, guest::sendKeypress);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, guest::sendKeypress);
    }

    private void update(final HostData hostData) {
        final int[] memory = new int[0x10000];
        for (int i = 0; i < hostData.getScreen().length; i++) {
            memory[16384 + i] = (int) hostData.getScreen()[i] & 0xff;
        }
        border.refresh(hostData.getBorderLines());
        display.refresh(memory, hostData.getFlashActive());
    }
}

