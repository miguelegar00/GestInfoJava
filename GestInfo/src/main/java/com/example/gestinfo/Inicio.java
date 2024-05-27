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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class Inicio extends Application {

    // Generamos el access token en la clase SalesforceTokenManager
    private SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    @Override
    public void start(Stage primaryStage) {

        String accessToken = tokenManager.getNewAccessToken();
        if (accessToken == null) {
            mostrarMensajeError("No se puede obtener el token de acceso.");
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
            if (verificarConexionSalesforce(accessToken, username, password)) {
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

        // Crear el texto "Crear cuenta"
        Label crearCuentaLabel = new Label("Crear cuenta");
        crearCuentaLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
        crearCuentaLabel.setOnMouseClicked(event -> {
            mostrarFormularioCrearCuenta(accessToken);
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
        GridPane.setMargin(accederButton, new Insets(0, 0, 0, 25));
        GridPane.setMargin(crearCuentaLabel, new Insets(10, 0, 0, 25));

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
    private boolean verificarConexionSalesforce(String accessToken, String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Mantener la consulta como la has especificado
            String query = String.format("SELECT+username__c,password__c+FROM+GestInfoUsers__c+WHERE+username__c='" + username + "'+AND+password__c='"+password+"'");
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

    // Método para mostrar el formulario de crear cuenta
    private void mostrarFormularioCrearCuenta(String accessToken) {
        Stage stage = new Stage();
        stage.setTitle("Crear Cuenta");

        // Crear campos de texto para el nombre de usuario y la contraseña
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");

        // Crear el botón Crear
        Button crearButton = new Button("Crear");
        crearButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        crearButton.setOnAction(event -> {
            // Obtener los valores de los campos del formulario de creación
            String username = usernameField.getText();
            String password = passwordField.getText();

            try {
                // Construir la URL del endpoint de Salesforce para crear una nueva cuenta
                String createUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/GestInfoUsers__c/";

                String data = "{\"username__c\": \"" + username + "\", " +
                        "\"password__c\": \"" + password + "\"}";

                // Ejecutar la solicitud POST a Salesforce para crear la nueva cuenta
                executePostRequest(createUrl, data, accessToken);

                // Mostrar mensaje de éxito
                mostrarMensajeInformacion("Cuenta creada exitosamente.");

                // Cerrar la ventana de creación después de crear la nueva cuenta en Salesforce
                stage.close();

            } catch (IOException e) {
                e.printStackTrace();
                mostrarMensajeError("Error al crear la cuenta: " + e.getMessage());
            }
        });

        VBox vbox = new VBox(10, usernameField, passwordField,crearButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(25));

        Scene scene = new Scene(vbox, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    // Método para ejecutar una solicitud POST
    public void executePostRequest(String url, String data, String accessToken) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + accessToken);

            // Configurar el cuerpo de la solicitud
            StringEntity entity = new StringEntity(data);
            httpPost.setEntity(entity);

            // Ejecutar la solicitud y obtener la respuesta
            HttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            // Verificar el código de estado de la respuesta
            if (statusCode == 200 || statusCode == 201) {
                // La operación POST se realizó correctamente
                // Aquí puedes manejar la respuesta si es necesario
            } else {
                throw new IOException("Error al crear la cuenta. Código de respuesta HTTP: " + statusCode);
            }
        }
    }

    // Método para obtener el ID del RecordType (simulado, deberías obtenerlo de tu instancia de Salesforce)
    private String getRecordTypeId(String recordType) {
        // Lógica para obtener el ID del RecordType
        return "0123A000000K0XY"; // Ejemplo, reemplazar con la lógica real
    }

    // Método para mostrar un mensaje de error
    private void mostrarMensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Método para mostrar un mensaje de información
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
