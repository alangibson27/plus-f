package com.socialthingy.qaopm.spectrum;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Optional;

public class UIBuilder {
    public static final int BORDER = 16;
    public static final int SCREEN_WIDTH = 256;
    public static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    public static final int DISPLAY_HEIGHT = ULA.SCREEN_HEIGHT + (BORDER * 2);

    public static void buildUI(
        final Stage primaryStage,
        final JavaFXDisplay display,
        final JavaFXBorder border,
        final MenuBar menuBar
    ) {
        final ImageView borderImage = new ImageView(border.getBorder());
        borderImage.setFitWidth(DISPLAY_WIDTH * 2);
        borderImage.setFitHeight(DISPLAY_HEIGHT * 2);

        final ImageView screenImage = new ImageView(display.getScreen());
        screenImage.setFitHeight(ULA.SCREEN_HEIGHT * 2);
        screenImage.setFitWidth(SCREEN_WIDTH * 2);

        final StackPane sp = new StackPane(borderImage, screenImage);

        BorderPane root = new BorderPane();
        root.setBottom(sp);
        root.setTop(menuBar);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
    }

    public static void registerMenuItem(
        final Menu menu,
        final String name,
        final Optional<KeyCode> accelerator,
        final EventHandler<ActionEvent> action
    ) {
        final MenuItem item = new MenuItem(name);
        item.setOnAction(action);
        accelerator.ifPresent(a ->
                item.setAccelerator(new KeyCodeCombination(a, KeyCombination.ALT_DOWN))
        );
        menu.getItems().add(item);
    }

}
