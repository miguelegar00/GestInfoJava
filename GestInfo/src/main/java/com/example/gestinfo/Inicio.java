package com.example.gestinfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Inicio extends Application {

    private SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    @SuppressWarnings("exports")
    @Override
    public void start(Stage primaryStage) {

        String accessToken = tokenManager.getNewAccessToken();
        if (accessToken == null) {
            mostrarMensajeError("No se puede obtener el token de acceso.");
            return;
        }

        Image image = new Image("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Salesforce.com_logo.svg/2560px-Salesforce.com_logo.svg.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button accederButton = new Button("Acceder");
        accederButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-color: transparent; -fx-border-radius: 5;");
        accederButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (verificarConexionSalesforce(accessToken, username, password)) {
                SalesforceOAuth salesforceOAuth = new SalesforceOAuth();
                try {
                    salesforceOAuth.start(new Stage());
                    primaryStage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mostrarMensajeError("Credenciales incorrectas.");
            }
        });

        Label crearCuentaLabel = new Label("Crear cuenta");
        crearCuentaLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
        crearCuentaLabel.setOnMouseClicked(event -> {
            mostrarFormularioCrearCuenta(accessToken);
        });

        Label olvidasteContrasenaLabel = new Label("Olvidaste tu contraseña");
        olvidasteContrasenaLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
        olvidasteContrasenaLabel.setOnMouseClicked(event -> {
            mostrarFormularioOlvidasteContrasena(accessToken);
        });

        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.add(usernameField, 0, 0);
        loginGrid.add(passwordField, 0, 1);
        loginGrid.add(accederButton, 0, 2);
        loginGrid.add(crearCuentaLabel, 0, 3);

        GridPane.setMargin(accederButton, new Insets(10, 0, 0, 25));
        GridPane.setMargin(crearCuentaLabel, new Insets(10, 0, 0, 25));

        HBox olvidasteContrasenaBox = new HBox(olvidasteContrasenaLabel);
        olvidasteContrasenaBox.setAlignment(Pos.BOTTOM_RIGHT);
        olvidasteContrasenaBox.setPadding(new Insets(10, 20, 30, 0));

        VBox mainVBox = new VBox(10);
        mainVBox.getChildren().addAll(loginGrid, olvidasteContrasenaBox);

        BorderPane root = new BorderPane();
        root.setCenter(imageView);
        root.setBottom(mainVBox);

        Scene scene = new Scene(root, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Soluciona Mi Deuda");

        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        primaryStage.show();
    }

    private boolean verificarConexionSalesforce(String accessToken, String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String query = String.format("SELECT+username__c,password__c,email__c+FROM+GestInfoUsers__c+WHERE+username__c='" + username + "'+AND+password__c='"+password+"'");
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
                    if (userRecord.has("password__c")) {
                        String storedPassword = userRecord.get("password__c").asText();
                        return storedPassword.equals(password);
                    } else {
                        System.err.println("password__c field is missing in the response");
                    }
                } else {
                    System.err.println("No records found or records field is missing in the response");
                }
                return false;
            } else {
                System.err.println("Failed to query Salesforce, status code: " + statusCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void mostrarFormularioCrearCuenta(String accessToken) {
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

                String data = "{\"username__c\": \"" + username + "\", " +
                        "\"email__c\": \"" + email + "\"," +
                        "\"password__c\": \"" + password + "\"}";

                executePostRequest(createUrl, data, accessToken);
                mostrarMensajeInformacion("Cuenta creada exitosamente.");
                stage.close();

            } catch (IOException e) {
                e.printStackTrace();
                mostrarMensajeError("Error al crear la cuenta: " + e.getMessage());
            }
        });

        VBox vbox = new VBox(10, usernameField, emailField, passwordField, crearButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private void mostrarFormularioOlvidasteContrasena(String accessToken) {
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

            if (recuperarContrasena(accessToken, username, email)) {
                mostrarMensajeInformacion("Las instrucciones para recuperar la contraseña han sido enviadas a su email.");
                stage.close();
            } else {
                mostrarMensajeError("No se pudo recuperar la contraseña. Verifique los datos ingresados.");
            }
        });

        VBox vbox = new VBox(10, usernameField, emailField, recuperarButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        Scene scene = new Scene(vbox, 400, 200);
        stage.setScene(scene);
        stage.show();
    }

    private boolean recuperarContrasena(String accessToken, String username, String email) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String query = String.format("SELECT+username__c,email__c,password__c+FROM+GestInfoUsers__c+WHERE+username__c='"+username+"'+AND+email__c='"+email+"'");
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
                    if (userRecord.has("email__c")) {
                        // Logic to send email with password reset instructions
                        // This part should contain your email-sending logic, for now we'll assume it succeeded
                        return true;
                    } else {
                    }
                } else {
                }
                return false;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void executePostRequest(String url, String data, String accessToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + accessToken);

            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200 || statusCode == 201) {
                // La operación POST se realizó correctamente
            } else {
                throw new IOException("Error al crear la cuenta. Código de respuesta HTTP: " + statusCode);
            }
        }
    }

    private void mostrarMensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarMensajeInformacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
