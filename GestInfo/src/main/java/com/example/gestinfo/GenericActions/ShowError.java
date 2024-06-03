package com.example.gestinfo.GenericActions;

import javafx.scene.control.Alert;

/**
 *
 * @author Miguel
 */
public class ShowError {

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
