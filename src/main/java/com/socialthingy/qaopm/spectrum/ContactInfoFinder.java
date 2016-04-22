package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.spectrum.remote.StunClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class ContactInfoFinder {

    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void getContactInfo(final ActionEvent ae) {
        final Alert progressDialog = new Alert(INFORMATION);
        progressDialog.getButtonTypes().setAll(ButtonType.CANCEL);
        progressDialog.setHeaderText("Getting contact info ... please wait");
        progressDialog.setTitle("Getting Contact Info");
        progressDialog.setGraphic(new ProgressIndicator(-1.0));
        progressDialog.show();

        executor.execute(() -> {
            final StunClient sc = new StunClient(7001);
            final Optional<InetSocketAddress> address = sc.discoverAddress();
            Platform.runLater(() -> displayResults(progressDialog, address));
        });
    }

    private static void displayResults(final Alert progressDialog, final Optional<InetSocketAddress> address) {
        progressDialog.hide();

        final Alert infoDialog = new Alert(address.isPresent() ? INFORMATION : WARNING);
        address.ifPresent(addr -> {
            infoDialog.setHeaderText("Contact info found");
            infoDialog.setContentText(String.format("Host: %s\nPort: %d", addr.getHostName(), addr.getPort()));
        });

        if (!address.isPresent()) {
            infoDialog.setHeaderText("Contact info not available");
            infoDialog.setContentText("It was not possible to find your contact info.\nNetwork play will not be possible.");
        }

        infoDialog.show();
    }

}
