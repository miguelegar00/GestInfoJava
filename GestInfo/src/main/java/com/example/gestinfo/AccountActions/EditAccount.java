package com.example.gestinfo.AccountActions;

import java.io.IOException;

import com.example.gestinfo.AccountActions.ShowAccounts.Account;
import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.ShowError;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class EditAccount {

    static void editar() {
        Account selectedAccount = ShowAccounts.accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            ShowError.showError("Debes seleccionar una cuenta para editar.");
            return;
        }

        Stage editStage = new Stage();
        VBox editRoot = new VBox();
        editRoot.setPadding(new Insets(20));
        editRoot.setSpacing(20);
        TextField newFirstNameField = new TextField(selectedAccount.getName());
        TextField newLastNameField = new TextField(selectedAccount.getLastName());
        TextField newPhoneField = new TextField(selectedAccount.getPhone());
        ComboBox<String> newClienteDeComboBox = new ComboBox<>();
        newClienteDeComboBox.getItems().addAll("LSO", "Programa");
        newClienteDeComboBox.setValue(selectedAccount.getCliente());

        Button saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        saveButton.setOnAction(event -> {
            String newFirstName = newFirstNameField.getText();
            String newLastName = newLastNameField.getText();
            String newPhone = newPhoneField.getText();
            String newClienteDe = newClienteDeComboBox.getValue();

            selectedAccount.setName(newFirstName);
            selectedAccount.setLastName(newLastName);
            selectedAccount.setPhone(newPhone);
            selectedAccount.setCliente(newClienteDe);

            try {
                String accountId = selectedAccount.getId();
                String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/" + accountId;

                String data = "{\"FirstName\": \"" + newFirstName + "\", \"LastName\": \"" + newLastName + "\", \"Phone\": \"" + newPhone + "\", \"Cliente_de__c\": \"" + newClienteDe + "\"}";

                SalesforceActions.executePatchRequest(updateUrl, data);

                editStage.close();

                ShowAccounts.accountTable.refresh();
            } catch (IOException e) {
                e.printStackTrace();
                ShowError.showError("Error al actualizar la cuenta: " + e.getMessage());
            }
        });

        HBox buttonContainer = new HBox(saveButton);
        buttonContainer.setAlignment(Pos.CENTER);

        editRoot.getChildren().addAll(
                new Label("Nombre:"),
                newFirstNameField,
                new Label("Apellido:"),
                newLastNameField,
                new Label("Teléfono:"),
                newPhoneField,
                new Label("Cliente de:"),
                newClienteDeComboBox,
                buttonContainer
        );

        Scene editScene = new Scene(editRoot, 400, 400);
        editStage.setScene(editScene);
        editStage.setMinWidth(400);
        editStage.setMinHeight(400);
        editStage.setTitle("Editar Cuenta");
        editStage.show();
    }
}
