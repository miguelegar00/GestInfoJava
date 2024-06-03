package com.example.gestinfo.UsersActions;

import java.io.IOException;

import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.ShowMessages;
import com.example.gestinfo.UsersActions.ShowUsers.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EditUsers {

    static void editUser() {
        User selectedUser = ShowUsers.userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Stage editStage = new Stage();
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.setTitle("Editar Usuario");
    
            VBox editRoot = new VBox(10);
            editRoot.setAlignment(Pos.CENTER);
            editRoot.setPadding(new Insets(20));
    
            TextField firstNameField = new TextField(selectedUser.getFirstName());
            TextField lastNameField = new TextField(selectedUser.getLastName());
    
            ComboBox<String> roleIdComboBox = new ComboBox<>();
            roleIdComboBox.setPromptText("Seleccionar rol");
            roleIdComboBox.getItems().addAll(ShowUsers.roleNames.values());
            roleIdComboBox.setValue(ShowUsers.roleNames.get(selectedUser.getUserRoleId()));
    
            Button saveButton = new Button("Guardar");
            saveButton.setOnAction(saveEvent -> {
                selectedUser.setFirstName(firstNameField.getText());
                selectedUser.setLastName(lastNameField.getText());
                selectedUser.setUserRoleId(ShowUsers.getKeyFromValue(ShowUsers.roleNames, roleIdComboBox.getValue()));
    
                // Actualizar el usuario seleccionado en Salesforce y en la tabla
                try {
                    String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                    String requestBody = "{\"FirstName\": \"" + selectedUser.getFirstName() + "\", \"LastName\": \"" + selectedUser.getLastName() + "\", \"UserRoleId\": \"" + selectedUser.getUserRoleId() + "\"}";
                    SalesforceActions.executePatchRequest(updateUrl, requestBody);
    
                    // Actualizar la tabla para reflejar los cambios
                    ShowUsers.userTable.refresh();
                    
                    editStage.close();
                } catch (IOException e) {
                    ShowMessages.mostrarMensajeError("Error al actualizar el usuario en Salesforce.");
                }
            });
    
            editRoot.getChildren().addAll(new Label("Nombre:"), firstNameField, new Label("Apellidos:"), lastNameField, new Label("Rol de usuario:"), roleIdComboBox, saveButton);
    
            Scene editScene = new Scene(editRoot, 300, 250);
            editStage.setScene(editScene);
            editStage.showAndWait();
        } else {
            ShowMessages.mostrarMensajeError("Por favor, selecciona un usuario para editar.");
        }
    }
}
