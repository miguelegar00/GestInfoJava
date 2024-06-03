package com.example.gestinfo.LoginAccountsActions;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class ResetPassword {

    public static void mostrarFormularioOlvidasteContrasena(String accessToken) {
        Stage stage = new Stage();
        stage.setTitle("Recuperar Contraseña");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        Button recuperarButton = new Button("Recuperar");
        recuperarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        recuperarButton.setOnAction(event -> {
            String username = usernameField.getText();
            String email = emailField.getText();

            String newPassword = recuperarContrasena(accessToken, username, email);
            if (newPassword != null) {
                showPasswordDialog(newPassword);
                stage.close();
            } else {
                ShowMessages.mostrarMensajeError("No se pudo recuperar la contraseña. Verifique los datos ingresados.");
            }
        });

        VBox vbox = new VBox(10, usernameField, emailField, recuperarButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private static void showPasswordDialog(String newPassword) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Contraseña Restablecida");
        alert.setHeaderText(null);
        Label label = new Label("Su contraseña ha sido restablecida. Su nueva contraseña es: " + newPassword);
        Button copyButton = new Button("Copiar");
        copyButton.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(newPassword);
            clipboard.setContent(content);
            ShowMessages.mostrarMensajeInformacion("Contraseña copiada al portapapeles.");
        });

        VBox vbox = new VBox(label, copyButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        alert.getDialogPane().setContent(vbox);
        alert.showAndWait();
    }

    private static String recuperarContrasena(String accessToken, String username, String email) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String query = String.format(
                "SELECT+Name,email__c,Id+FROM+GestInfoUsers__c+WHERE+Name='%s'+AND+email__c='%s'",
                URLEncoder.encode(username, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
            );
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
                        String newPassword = "Soluciona1.";

                        if (actualizarContrasena(accessToken, userId, newPassword)) {
                            return newPassword;
                        } else {
                            ShowMessages.mostrarMensajeError("Error al actualizar la contraseña en Salesforce.");
                        }
                    } else {
                        ShowMessages.mostrarMensajeError("No se encontró el ID del usuario.");
                    }
                } else {
                    ShowMessages.mostrarMensajeError("No se encontraron registros para los datos proporcionados.");
                }
                return null;
            } else {
                ShowMessages.mostrarMensajeError("Error al consultar Salesforce. Código de estado: " + statusCode);
                return null;
            }
        } catch (Exception e) {
            ShowMessages.mostrarMensajeError("Error al recuperar la contraseña: " + e.getMessage());
            return null;
        }
    }

    public static void mostrarFormularioCambiarContrasena(SalesforceTokenManager tokenManager) throws IOException {
        @SuppressWarnings("static-access")
        String accessToken = tokenManager.getNewAccessToken();
        String userId = getUserId(accessToken);
    
        if (userId == null) {
            ShowMessages.mostrarMensajeError("No se ha encontrado el usuario. Inicie sesión nuevamente.");
            return;
        }
    
        Stage stage = new Stage();
        stage.setTitle("Cambiar Contraseña");
    
        PasswordField newPasswordField = new PasswordField(); // Cambio aquí
        newPasswordField.setPromptText("Nueva Contraseña");
    
        PasswordField confirmPasswordField = new PasswordField(); // Cambio aquí
        confirmPasswordField.setPromptText("Confirmar Nueva Contraseña");
    
        Button cambiarButton = new Button("Cambiar");
        cambiarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        cambiarButton.setOnAction(event -> {
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
    
            if (!newPassword.equals(confirmPassword)) {
                ShowMessages.mostrarMensajeError("Las contraseñas no coinciden.");
                return;
            }
            if (accessToken == null) {
                ShowMessages.mostrarMensajeError("No se puede obtener el token de acceso.");
                return;
            }
    
            if (ResetPassword.actualizarContrasena(accessToken, userId, newPassword)) {
                ShowMessages.mostrarMensajeInformacion("Contraseña actualizada exitosamente.");
                stage.close();
            } else {
                ShowMessages.mostrarMensajeError("Error al actualizar la contraseña.");
            }
        });
    
        VBox vbox = new VBox(10, newPasswordField, confirmPasswordField, cambiarButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));
    
        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }
    

    private static String getUserId(String accessToken) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String username = System.getProperty("Name");
            String query = String.format(
                "SELECT+Id+FROM+GestInfoUsers__c+WHERE+Name='"+username+"'",
                URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
            );
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
                        return userRecord.get("Id").asText();
                    }
                }
            } else {
            }
        } catch (IOException e) {
        }
        return null;
    }

    private static boolean actualizarContrasena(String accessToken, String userId, String newPassword) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/GestInfoUsers__c/" + userId;
            String data = "{\"password__c\": \"" + newPassword + "\"}";

            HttpPatch httpPatch = new HttpPatch(updateUrl);
            httpPatch.addHeader("Content-Type", "application/json");
            httpPatch.addHeader("Authorization", "Bearer " + accessToken);

            StringEntity entity = new StringEntity(data);
            httpPatch.setEntity(entity);

            HttpResponse updateResponse = httpClient.execute(httpPatch);
            int updateStatusCode = updateResponse.getStatusLine().getStatusCode();

            return (updateStatusCode == 200 || updateStatusCode == 204);
        } catch (IOException e) {
            ShowMessages.mostrarMensajeError("Error al actualizar la contraseña: " + e.getMessage());
            return false;
        }
    }
}
