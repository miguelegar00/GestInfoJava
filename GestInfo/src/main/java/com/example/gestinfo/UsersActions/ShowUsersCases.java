package com.example.gestinfo.UsersActions;

import java.io.IOException;

import com.example.gestinfo.ShowCases;
import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.SalesforceTokenManager;
import com.example.gestinfo.GenericActions.ShowMessages;
import com.example.gestinfo.UsersActions.ShowUsers.User;

public class ShowUsersCases {

    static void showCases() {
        User selectedUser = ShowUsers.userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    
                    String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Subject,Status,Id+FROM+Case+WHERE+OwnerId='" + selectedUser.getId() + "'";
                    String bearerToken = SalesforceTokenManager.getNewAccessToken();
                    String response = SalesforceActions.executeQuery(queryUrl, bearerToken);

                    if (response.contains("\"totalSize\":0")) {
                        
                        ShowMessages.mostrarMensajeError("El usuario seleccionado no tiene ningún caso asignado.");
                    } else {
                        
                        ShowCases.abrirVentanaCasos(selectedUser);
                    }
                } catch (IOException e) {
                    ShowMessages.mostrarMensajeError("Error al verificar los casos del usuario.");
                }
            } else {
                ShowMessages.mostrarMensajeError("Por favor, selecciona un usuario para ver los casos.");
            }
    }
}
