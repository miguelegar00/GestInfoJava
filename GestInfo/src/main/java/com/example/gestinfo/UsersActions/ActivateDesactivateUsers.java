package com.example.gestinfo.UsersActions;

import java.io.IOException;
import java.util.Optional;

import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.ShowMessages;
import com.example.gestinfo.UsersActions.ShowUsers.User;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ActivateDesactivateUsers {

    static void activateUser() {
        User selectedUser = ShowUsers.userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {

                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de activación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres activar a este usuario?");

                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {

                    selectedUser.setIsActive(true);
                    
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": true}";
                        SalesforceActions.executePatchRequest(updateUrl, requestBody);
                        ShowUsers.userTable.refresh();
                    } catch (IOException e) {
                        ShowMessages.mostrarMensajeError("Error al actualizar el usuario en Salesforce.");
                    }
                }
            } else {
                ShowMessages.mostrarMensajeError("Por favor, selecciona un usuario para activar.");
            }
    }

    static void desactivateUser() {
        User selectedUser = ShowUsers.userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de desactivación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres desactivar a este usuario?");

                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    
                    selectedUser.setIsActive(false);
                    
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": false}";
                        SalesforceActions.executePatchRequest(updateUrl, requestBody);
                        
                        ShowUsers.userTable.refresh();
                    } catch (IOException e) {
                        ShowMessages.mostrarMensajeError("Error al actualizar el usuario en Salesforce.");
                    }
                }
            } else {
                
                ShowMessages.mostrarMensajeError("Por favor, selecciona un usuario para desactivar.");
            }
    }
}
