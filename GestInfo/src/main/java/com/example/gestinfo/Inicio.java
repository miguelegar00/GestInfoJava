package com.example.gestinfo;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.example.gestinfo.AccountActions.ShowAccounts;
import com.example.gestinfo.GenericActions.SalesforceTokenManager;
import com.example.gestinfo.GenericActions.ShowMessages;
import com.example.gestinfo.LoginAccountsActions.CreateLoginAccount;
import com.example.gestinfo.LoginAccountsActions.ResetPassword;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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


public class Inicio extends Application {

    private SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    @SuppressWarnings({ "exports", "static-access" })
    @Override
    public void start(Stage primaryStage) {

        String accessToken = tokenManager.getNewAccessToken();
        if (accessToken == null) {
            ShowMessages.mostrarMensajeError("No se puede obtener el token de acceso.");
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
                ShowAccounts salesforceOAuth = new ShowAccounts();
                try {
                    salesforceOAuth.start(new Stage());
                    primaryStage.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ShowMessages.mostrarMensajeError("Credenciales incorrectas.");
            }
        });

        Label crearCuentaLabel = new Label("Crear cuenta");
        crearCuentaLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
        crearCuentaLabel.setOnMouseClicked(event -> {
            CreateLoginAccount.mostrarFormularioCrearCuenta(accessToken);
        });

        Label olvidasteContrasenaLabel = new Label("Olvidaste tu contraseña");
        olvidasteContrasenaLabel.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
        olvidasteContrasenaLabel.setOnMouseClicked(event -> {
            ResetPassword.mostrarFormularioOlvidasteContrasena(accessToken);
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
        primaryStage.setTitle("GestInfo");

        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        primaryStage.show();
    }

    private boolean verificarConexionSalesforce(String accessToken, String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String query = String.format("SELECT+Name,password__c,email__c+FROM+GestInfoUsers__c+WHERE+Name='" + username + "'+AND+password__c='"+password+"'");
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
                    if (userRecord.has("password__c") && userRecord.has("Name")) {
                        String storedPassword = userRecord.get("password__c").asText();
                        if (storedPassword.equals(password)) {
                            //Guardo el usuario en una variable de entorno para usarlo en la query de cambio de contraseña una vez que hemos iniciado sesión
                            String storedUsername = userRecord.get("Name").asText();
                            System.setProperty("Name", storedUsername);
                            return true;
                        } else {
                        }
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

    public static void main(String[] args) {
        launch(args);
    }
}
