package com.example.gestinfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Inicio extends Application {

    //Generamos el access token en la clase SalesforceTokenManager
    private SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    // Variables para almacenar las credenciales y el resultado de la consulta
    private static String USERNAME;
    private static final String PASSWORD = "1";




    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {

        String accessToken = tokenManager.getNewAccessToken();
        if (accessToken == null) {
            mostrarMensajeError("No se puede obtener el token de acceso.");
            return;
        }

        if (!verificarConexionSalesforce(accessToken)) {
            mostrarMensajeError("No se puede conectar a Salesforce.");
            return;
        }

        // Crear la imagen y el ImageView
        Image image = new Image("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Salesforce.com_logo.svg/2560px-Salesforce.com_logo.svg.png");
        ImageView imageView = new ImageView(image);

        // Configurar el tamaño del ImageView
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Crear campos de texto para el nombre de usuario y la contraseña
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Crear el botón Acceder
        Button accederButton = new Button("Acceder");
        accederButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-color: transparent; -fx-border-radius: 5;");
        accederButton.setOnAction(event -> {

            String username = usernameField.getText();
            String password = passwordField.getText();

            // Verificar las credenciales
            if (username.equals(USERNAME) && password.equals(PASSWORD)) {
                // Ejecutar la clase SalesforceOAuth si la conexión es exitosa
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

        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.add(usernameField, 0, 0);
        loginGrid.add(passwordField, 0, 1);
        loginGrid.add(accederButton, 0, 2);
        GridPane.setMargin(accederButton, new Insets(0, 0, 0, 25));

        // Configurar el contenedor principal
        BorderPane root = new BorderPane();
        root.setCenter(imageView);
        root.setBottom(loginGrid);

        // Configurar la escena y mostrar la ventana
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Soluciona Mi Deuda");

        // Establecer el icono de la ventana
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        primaryStage.show();
    }

    // Método para verificar la conexión a Salesforce utilizando el token de acceso
    private boolean verificarConexionSalesforce(String accessToken) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query?q=SELECT+Alias+FROM+User+WHERE+Id='005So000000s4wHIAQ'");
            httpGet.addHeader("Authorization", "Bearer " + accessToken);

            HttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 200 && statusCode < 300) {
                String responseBody = EntityUtils.toString(response.getEntity());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                USERNAME = jsonNode.get("records").get(0).get("Alias").asText();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método para mostrar un mensaje de error
    private void mostrarMensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
