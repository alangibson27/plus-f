package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.spectrum.remote.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.socialthingy.qaopm.spectrum.UIBuilder.registerMenuItem;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.input.KeyCode.*;

public class JavaFXGuest extends Application {

    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXGuest() {
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        UIBuilder.buildUI(primaryStage, display, border, getMenuBar());

        primaryStage.setTitle("QAOPM Spectrum Emulator - GUEST");
        primaryStage.show();
//        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, guest::sendKeypress);
//        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, guest::sendKeypress);
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));
        registerMenuItem(fileMenu, "Get contact info ...", Optional.of(I), this::getContactInfo);

        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void getContactInfo(final ActionEvent ae) {
        final Alert alert = new Alert(INFORMATION);
        alert.getButtonTypes().setAll(ButtonType.CANCEL);
        alert.setHeaderText("Getting contact info ... please wait");
        alert.setTitle("Getting Contact Info");
        alert.setGraphic(new ProgressIndicator(-1.0));
        alert.show();

        executor.execute(() -> {
            final StunClient sc = new StunClient(7001);
            final Optional<InetSocketAddress> address = sc.discoverAddress();
            Platform.runLater(() -> {
                alert.hide();

                final Alert completedAlert = new Alert(address.isPresent() ? INFORMATION : WARNING);
                address.ifPresent(addr -> {
                    completedAlert.setHeaderText("Contact info found");
                    completedAlert.setContentText(String.format("Host: %s\nPort: %d", addr.getHostName(), addr.getPort()));
                });

                if (!address.isPresent()) {
                    completedAlert.setHeaderText("Contact info not available");
                    completedAlert.setContentText("It was not possible to find your contact info.\nNetwork play will not be possible.");
                }

                completedAlert.show();
            });
        });
    }

    private void update(final SpectrumState hostData) {
        final int[] memory = new int[0x10000];
        for (int i = 0; i < hostData.getScreen().length; i++) {
            memory[16384 + i] = (int) hostData.getScreen()[i] & 0xff;
        }
        border.refresh(hostData.getBorderLines());
        display.refresh(memory, hostData.isFlashActive());
    }
}

