package com.example.gestinfo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesforceOAuth extends Application {

    private String selectedId;
    private TableView<Account> accountTable;

    @Override
    public void start(Stage primaryStage) {

        // Configurar el layout principal
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // Crear la tabla para mostrar los IDs y los Names
        accountTable = new TableView<>();

        // Configurar las columnas de la tabla
        TableColumn<Account, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<Account, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        accountTable.getColumns().addAll(idColumn, nameColumn);

        // Realizar la consulta y mostrar los resultados en la tabla
        try {
            executeAndDisplayResults();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al realizar la consulta: " + e.getMessage());
        }

        // Configurar el botón "Generar Documento"
        Button generarDoc = new Button("Generar Documento");
        generarDoc.setOnAction(event -> generarDocumento());

        // Configurar el botón "Editar"
        Button editarButton = new Button("Editar");
        editarButton.setOnAction(event -> editar());

        // Crear un contenedor para los botones
        HBox buttonBox = new HBox(generarDoc, editarButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Agregar la tabla y los botones al layout principal
        root.getChildren().addAll(accountTable, buttonBox);

        // Configurar la escena
        Scene scene = new Scene(root);

        // Mostrar la ventana
        primaryStage.setTitle("GestInfo");
        primaryStage.setScene(scene);
        
        // Maximizar la ventana
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    private void executeAndDisplayResults() throws IOException {
        
        // URL de la consulta
        String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,Name+FROM+Account";

        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQLlBTUj5KixcUs.erlhlA7aG.lOajBopNjz688SMEWbjd1jRcv.5tXJoC77U9pvrlEXhpl3dgmA2EHyYGpLZyQPkVzDZ";

        // Realizar la consulta
        String response = executeQuery(queryUrl, bearerToken);

        // Procesar la respuesta y mostrar los IDs y los Names en la tabla
        ObservableList<Account> accountList = FXCollections.observableArrayList();

        Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"Name\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            String id = matcher.group(1);
            String name = matcher.group(2);
            accountList.add(new Account(id, name));
        }

        accountTable.setItems(accountList);
    }

    private void generarDocumento() {
        if (selectedId == null) {
            showError("Debes seleccionar una cuenta.");
            return;
        }

        // Ejecutar la clase OpenUrlExample
        OpenUrlExample.openUrlWithId(selectedId);

        showSuccess("Documento generado correctamente.");
    }

    private String executeQuery(String url, String bearerToken) throws IOException {
        URL queryUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) queryUrl.openConnection();
        connection.setRequestMethod("GET");

        // Agregar el token de portador al encabezado de autorización
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);

        // Realizar la solicitud GET
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        VBox root = (VBox) accountTable.getParent();
        root.getChildren().add(errorLabel);
    }

    private void showSuccess(String message) {
        Label successLabel = new Label(message);
        VBox root = (VBox) accountTable.getParent();
        root.getChildren().add(successLabel);
    }

    private void editar() {
        // Tu código para la funcionalidad de editar...
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Account {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;

        public Account(String id, String name) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }
    }
}
