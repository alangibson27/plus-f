package com.socialthingy.plusf.spectrum.dialog;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static javafx.scene.control.Alert.AlertType.INFORMATION;

public class CancelableProgressDialog {

    private static final ScheduledExecutorService taskExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void shutdown() {
        taskExecutor.shutdownNow();
    }

    public static <T> void show(
        final Task<T> task,
        final String initialMessage,
        final String title,
        final Consumer<T> onSuccess
    ) {
        final Alert progressDialog = new Alert(INFORMATION);
        progressDialog.getButtonTypes().setAll(ButtonType.CANCEL);
        progressDialog.setHeaderText(initialMessage);
        progressDialog.setTitle(title);
        progressDialog.setGraphic(new ProgressIndicator(-1.0));

        progressDialog.headerTextProperty().bind(task.messageProperty());

        progressDialog.setOnCloseRequest(de -> task.cancel(true));
        progressDialog.show();

        taskExecutor.execute(task);

        task.setOnFailed(e -> {
            progressDialog.hide();
            ErrorDialog.show(
                    "Connection Error",
                    e.getSource().getException().getMessage(),
                    Optional.of(e.getSource().getException())
            );
        });
        task.setOnCancelled(e -> progressDialog.hide());
        task.setOnSucceeded(wse -> {
            progressDialog.close();
            final T result = (T) wse.getSource().getValue();
            onSuccess.accept(result);
        });
    }
}
