package com.example.gestinfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

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

    // Credenciales de inicio de sesión
    private static final String USERNAME = "1";
    private static final String PASSWORD = "1";

    // Bearer token de acceso a Salesforce
    private static final String SALESFORCE_BEARER_TOKEN = "00DUB000001QzdZ!AQEAQNMyWTCXFPvmxHL6bwf9P7CmC8QOBcyaq16C6DfaeFWjqUQ8UsoqmKh4S9ro.t8vC2pJ673RSHDhb_GnlMICPlD0jOXH";

    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        // Crear la imagen y el ImageView
        Image image = new Image("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Salesforce.com_logo.svg/2560px-Salesforce.com_logo.svg.png");
        ImageView imageView = new ImageView(image);

        // Configurar el tamaño del ImageView
        imageView.setFitWidth(300); // Ancho de la imagen
        imageView.setFitHeight(200); // Alto de la imagen
        imageView.setPreserveRatio(true); // Mantener la proporción de la imagen al cambiar el tamaño

        // Crear campos de texto para el nombre de usuario y la contraseña
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // Crear el botón Acceder
        Button accederButton = new Button("Acceder");
        accederButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-color: transparent; -fx-border-radius: 5;");
        accederButton.setOnAction(event -> {
            // Obtener el nombre de usuario y la contraseña ingresados
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Verificar las credenciales
            if (username.equals(USERNAME) && password.equals(PASSWORD)) {
                // Verificar la conexión a Salesforce utilizando el token de acceso
                boolean conexionExitosa = verificarConexionSalesforce(SALESFORCE_BEARER_TOKEN);
                if (conexionExitosa) {
                    // Ejecutar la clase SalesforceOAuth si la conexión es exitosa
                    SalesforceOAuth salesforceOAuth = new SalesforceOAuth();
                    try {
                        salesforceOAuth.start(new Stage());
                        primaryStage.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // Mostrar un mensaje de error si la conexión falla
                    mostrarMensajeError("No se puede conectar a Salesforce.");
                }
            } else {
                // Mostrar un mensaje de error si las credenciales son incorrectas
                mostrarMensajeError("Credenciales incorrectas.");
            }
        });

        // Organizar los campos de texto y el botón en un GridPane
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));
        loginGrid.add(usernameField, 0, 0);
        loginGrid.add(passwordField, 0, 1);
        loginGrid.add(accederButton, 0, 2);
        // Establecer el margen del botón para moverlo 10 píxeles a la derecha
        GridPane.setMargin(accederButton, new Insets(0, 0, 0, 25));


        // Configurar el contenedor principal
        BorderPane root = new BorderPane();
        root.setCenter(imageView); // Colocar la imagen en el centro
        root.setBottom(loginGrid); // Colocar el formulario debajo de la imagen

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
        try {
            // Crear cliente HTTP
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // Crear solicitud GET a una URL de Salesforce
            HttpGet httpGet = new HttpGet("https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query?q=SELECT+Id+FROM+User");

            // Agregar el token de acceso al encabezado de autorización
            httpGet.addHeader("Authorization", "Bearer " + accessToken);

            // Ejecutar la solicitud y obtener la respuesta
            HttpResponse response = httpClient.execute(httpGet);

            // Verificar el código de estado de la respuesta para determinar si la conexión es exitosa
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode >= 200 && statusCode < 300;
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