package com.example.gestinfo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesforceOAuth extends Application {

    private TableView<Account> accountTable;

    @Override
    public void start(Stage primaryStage) {

        // Configurar el layout principal
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setSpacing(20);

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
        String bearerToken = "00DUB000001QzdZ!AQEAQMH4uyhDUJMG6GCPM_MBjtwEFCCVlb9vfB0KCSMpchVnR1KRNHEg6bbnggNqGn146qlH7eJ1IxaBW3SVTfqaM.48deeL";

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
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showError("Debes seleccionar una cuenta.");
            return;
        }

        // Ejecutar la clase OpenUrlExample
        OpenUrlExample.openUrlWithId(selectedAccount.getId());

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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void executePatchRequest(String url, String data) throws IOException {
        URL updateUrl = new URL(url);
        String bearerToken = "00DUB000001QzdZ!AQEAQMH4uyhDUJMG6GCPM_MBjtwEFCCVlb9vfB0KCSMpchVnR1KRNHEg6bbnggNqGn146qlH7eJ1IxaBW3SVTfqaM.48deeL";
        HttpURLConnection connection = (HttpURLConnection) updateUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setDoOutput(true);

        // Escribir los datos a enviar en la solicitud
        try (OutputStream outputStream = connection.getOutputStream()) {
            byte[] input = data.getBytes("utf-8");
            outputStream.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException("Error al actualizar la cuenta. Código de respuesta HTTP: " + responseCode);
        }
    }

    private void editar() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showError("Debes seleccionar una cuenta para editar.");
            return;
        }
    
        // Abrir una nueva ventana para el formulario de edición
        Stage editStage = new Stage();
        // Crear el formulario de edición
        // Por simplicidad, aquí puedes crear un nuevo Scene o cargar un FXML para el formulario de edición
        VBox editRoot = new VBox();
        editRoot.setPadding(new Insets(20));
        editRoot.setSpacing(20);
        TextField newNameField = new TextField(selectedAccount.getName());
        Button saveButton = new Button("Guardar");
        saveButton.setOnAction(event -> {
            String newAccountName = newNameField.getText();
            selectedAccount.setName(newAccountName);
            try {
                // Construir la URL del endpoint de Salesforce para la cuenta específica
                String accountId = selectedAccount.getId();
                String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/" + accountId;
                
                // Construir los datos a enviar en la solicitud PATCH
                String data = "{\"Name\": \"" + newAccountName + "\"}";
                
                // Ejecutar la solicitud PATCH a Salesforce para actualizar los datos
                executePatchRequest(updateUrl, data);
                
                // Cerrar la ventana de edición después de actualizar los datos en Salesforce
                editStage.close();
                
                // Actualizar la tabla para reflejar los cambios
                accountTable.refresh();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error al actualizar la cuenta: " + e.getMessage());
            }
        });
    
        // Manejar el evento de presionar la tecla "Enter" en el TextField
        newNameField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                // Simular un clic en el botón "Guardar" al presionar Enter
                saveButton.fire();
            }
        });
    
        editRoot.getChildren().addAll(new Label("Nuevo Nombre:"), newNameField, saveButton);
        Scene editScene = new Scene(editRoot, 400, 300); // Establecer el ancho y alto de la escena
        editStage.setScene(editScene);
        editStage.setMinWidth(400); // Establecer el ancho mínimo de la ventana
        editStage.setMinHeight(300); // Establecer la altura mínima de la ventana
        editStage.setTitle("Editar Cuenta");
        editStage.show();
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

        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }
    }
}
