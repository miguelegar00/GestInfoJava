package com.example.gestinfo.LoginAccountsActions;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.ShowMessages;

/**
 *
 * @author Miguel
 */
public class CreateLoginAccount {

    public static void mostrarFormularioCrearCuenta(String accessToken) {
        Stage stage = new Stage();
        stage.setTitle("Crear Cuenta");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");

        Button crearButton = new Button("Crear");
        crearButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        crearButton.setOnAction(event -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            try {
                String createUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/GestInfoUsers__c/";

                String data = "{\"Name\": \"" + username + "\", " +
                        "\"email__c\": \"" + email + "\"," +
                        "\"password__c\": \"" + password + "\"}";

                SalesforceActions.executePostRequest(createUrl, data, accessToken);
                ShowMessages.mostrarMensajeInformacion("Cuenta creada exitosamente.");
                stage.close();

            } catch (IOException e) {
                e.printStackTrace();
                ShowMessages.mostrarMensajeError("Error al crear la cuenta: " + e.getMessage());
            }
        });

        VBox vbox = new VBox(10, usernameField, emailField, passwordField, crearButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }
}
