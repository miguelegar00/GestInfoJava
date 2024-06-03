package com.example.gestinfo.LoginAccountsActions;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.example.gestinfo.GenericActions.SalesforceTokenManager;
import com.example.gestinfo.GenericActions.ShowMessages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class ChangeUsername {

    public static void mostrarFormularioCambiarUsername(SalesforceTokenManager tokenManager) throws IOException {
        String username = System.getProperty("Name");
        
        if (username == null) {
            ShowMessages.mostrarMensajeError("No se ha encontrado el usuario. Inicie sesión nuevamente.");
            return;
        }
        
        Stage stage = new Stage();
        stage.setTitle("Cambiar nombre del usuario");
    
        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Nuevo nombre");
    
        Button cambiarButton = new Button("Cambiar");
        cambiarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        cambiarButton.setOnAction(event -> {
            String newUsername = newUsernameField.getText();
    
            @SuppressWarnings("static-access")
            String accessToken = tokenManager.getNewAccessToken();
            if (accessToken == null) {
                ShowMessages.mostrarMensajeError("No se puede obtener el token de acceso.");
                return;
            }
   
            if (actualizarUsername(accessToken, username, newUsername)) {
                ShowMessages.mostrarMensajeInformacion("Nombre de usuario actualizado exitosamente.");
                stage.close();
            } else {
                ShowMessages.mostrarMensajeError("Error al actualizar el nombre del usuario.");
            }
        });
    
        VBox vbox = new VBox(10, newUsernameField, cambiarButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));
    
        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private static boolean actualizarUsername(String accessToken, String username, String newUsername) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            
            String query = String.format("SELECT+Id+FROM+GestInfoUsers__c+WHERE+Name='"+username+"'");
            HttpGet httpGet = new HttpGet("https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query?q=" + query);
            httpGet.addHeader("Authorization", "Bearer " + accessToken);
    
            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
    
                if (jsonNode.has("records") && jsonNode.get("records").size() > 0) {
                    JsonNode userRecord = jsonNode.get("records").get(0);
                    if (userRecord.has("Id")) {
                        String userId = userRecord.get("Id").asText(); 
    
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/GestInfoUsers__c/" + userId;
                        String data = "{\"Name\": \"" + newUsername + "\"}";
    
                        HttpPatch httpPatch = new HttpPatch(updateUrl);
                        httpPatch.addHeader("Content-Type", "application/json");
                        httpPatch.addHeader("Authorization", "Bearer " + accessToken);
    
                        StringEntity entity = new StringEntity(data);
                        httpPatch.setEntity(entity);
    
                        HttpResponse updateResponse = httpClient.execute(httpPatch);
                        int updateStatusCode = updateResponse.getStatusLine().getStatusCode();
    
                        return (updateStatusCode == 200 || updateStatusCode == 204);
                    } else {
                        ShowMessages.mostrarMensajeError("No se encontró el ID del usuario.");
                    }
                } else {
                    ShowMessages.mostrarMensajeError("No se encontraron registros para los datos proporcionados.");
                }
            } else {
                ShowMessages.mostrarMensajeError("Error al consultar Salesforce. Código de estado: " + statusCode);
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            ShowMessages.mostrarMensajeError("Error al actualizar el nombre del usuairo: " + e.getMessage());
            return false;
        }
    }
}
