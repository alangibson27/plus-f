package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.spectrum.display.Icons;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import static com.socialthingy.plusf.spectrum.display.Icons.iconFrom;

public class TapeControls {

    private static final String FULL_OPACITY = "-fx-opacity: 1.0;";
    private static final String HALF_OPACITY = "-fx-opacity: 0.5;";

    public static HBox getTapeControls(
        final MenuItem playTapeItem,
        final MenuItem stopTapeItem,
        final MenuItem rewindTapeToStartItem,
        final BooleanProperty tapePlayingProperty
    ) {
        final ImageView tapeIcon = iconFrom(Icons.tape);
        final StackPane playButton = createButton(Icons.play, playTapeItem);
        final StackPane stopButton = createButton(Icons.stop, stopTapeItem);
        final StackPane rewindButton = createButton(Icons.rewindToStart, rewindTapeToStartItem);

        final HBox tapeControls = new HBox(2);
        final Insets insets = new Insets(2, 2, 2, 2);
        HBox.setMargin(tapeIcon, insets);
        HBox.setMargin(playButton, insets);
        HBox.setMargin(stopButton, insets);
        HBox.setMargin(rewindButton, insets);
        tapeControls.getChildren().addAll(tapeIcon, playButton, stopButton, rewindButton);
        tapeControls.setAlignment(Pos.CENTER);

        tapePlayingProperty.addListener((observable, wasPlaying, isPlaying) -> {
            if (isPlaying) {
                tapeIcon.setImage(Icons.tapePlaying);
            } else {
                tapeIcon.setImage(Icons.tape);
            }
        });

        return tapeControls;
    }

    private static StackPane createButton(final Image icon, final MenuItem pairedMenuItem) {
        final StackPane buttonStack = new StackPane();
        buttonStack.setPrefWidth(30.0);
        buttonStack.setPrefHeight(30.0);
        buttonStack.setStyle("-fx-border-width: 1px; -fx-border-color: #000000;");

        final ImageView iconImage = Icons.iconFrom(icon);
        iconImage.setStyle("-fx-opacity: 0.5;");

        final Label playText = new Label(pairedMenuItem.getAccelerator().getDisplayText());

        StackPane.setAlignment(iconImage, Pos.TOP_CENTER);
        StackPane.setAlignment(playText, Pos.BOTTOM_CENTER);
        buttonStack.getChildren().add(iconImage);
        buttonStack.getChildren().add(playText);

        buttonStack.setOnMouseClicked(event -> pairedMenuItem.getOnAction().handle(adaptEvent(event)));
        pairedMenuItem.disableProperty().addListener((observable, wasDisabled, isDisabled) -> {
            if (isDisabled) {
                iconImage.setStyle(HALF_OPACITY);
            } else {
                iconImage.setStyle(FULL_OPACITY);
            }
        });

        return buttonStack;
    }

    private static ActionEvent adaptEvent(final MouseEvent event) {
        return new ActionEvent(event.getSource(), event.getTarget());
    }
}
