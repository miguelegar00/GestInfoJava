package com.example.gestinfo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

public class SalesforceOAuth extends Application {

    private TableView<Account> accountTable;

    @Override
    public void start(Stage primaryStage) {

        // Configurar el layout principal
        VBox root = new VBox();
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #f0f0f0;");

        MenuBar menuBar = new MenuBar();

        // Menú "Salir"
        Menu salirMenu = new Menu("Salir");
        MenuItem cerrarSesionItem = new MenuItem("Salir del programa");
        cerrarSesionItem.setOnAction(event -> cerrarSesion());
        salirMenu.getItems().add(cerrarSesionItem);

        // Menú "Nuevo" con submenú "Crear nueva cuenta"
        Menu nuevoMenu = new Menu("Nuevo");
        MenuItem crearCuentaItem = new MenuItem("Crear nueva cuenta");
        crearCuentaItem.setOnAction(event -> crearCuentaNueva());
        nuevoMenu.getItems().add(crearCuentaItem);

        // Agregar los menús al MenuBar
        menuBar.getMenus().addAll(salirMenu, nuevoMenu);

        // Crear la tabla para mostrar los IDs y los Names
        accountTable = new TableView<>();
        accountTable.setStyle("-fx-background-color: white;");

        // Configurar las columnas de la tabla
        TableColumn<Account, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<Account, String> nameColumn = new TableColumn<>("Nombre");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Account, String> lastNameColumn = new TableColumn<>("Apellidos");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        TableColumn<Account, String> clienteDeColumn = new TableColumn<>("Cliente de");
        clienteDeColumn.setCellValueFactory(cellData -> cellData.getValue().clienteDeProperty());

        TableColumn<Account, String> phoneColumn = new TableColumn<>("Teléfono");
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        accountTable.getColumns().addAll(idColumn, nameColumn, lastNameColumn, clienteDeColumn, phoneColumn);

        // Realizar la consulta y mostrar los resultados en la tabla
        try {
            executeAndDisplayResults();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al realizar la consulta: " + e.getMessage());
        }

        // Configurar el botón "Generar Documentos"
        Button generarDoc = new Button("Generar Documentos");
        generarDoc.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        generarDoc.setOnAction(event -> generarDocumento());

        // Configurar el botón "Editar"
        Button editarButton = new Button("Editar");
        editarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        editarButton.setOnAction(event -> editar());

        // Crear un contenedor para los botones
        HBox buttonBox = new HBox(generarDoc, editarButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Agregar la tabla y los botones al layout principal
        root.getChildren().addAll(menuBar, accountTable, buttonBox);

        // Configurar la escena
        Scene scene = new Scene(root);

        // Mostrar la ventana
        primaryStage.setTitle("GestInfo");
        primaryStage.setScene(scene);

        // Maximizar la ventana
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    private String decodeString(String encodedString) {
        try {
            return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodedString;
        }
    }

    private void executeAndDisplayResults() throws IOException {
        // URL de la consulta
        String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,Cliente_de__c,Phone+FROM+Account+WHERE+Id+!=+null";

        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQHt6T.S.CnVpDb0wsACKEacY7O2Dvl8wL_I1S5KhP.LfjTxcTNmxvMx2snj5JXFK1CkUv7kNG.kd1j9BICF.utVQZ3E2";

        // Realizar la consulta
        String response = executeQuery(queryUrl, bearerToken);

        // Procesar la respuesta y mostrar los IDs y los Names en la tabla
        ObservableList<Account> accountList = FXCollections.observableArrayList();

        Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"FirstName\"\\s*:\\s*\"(.*?)\",\"LastName\"\\s*:\\s*\"(.*?)\",\"Cliente_de__c\"\\s*:\\s*\"(.*?)\",\"Phone\"\\s*:\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            String id = matcher.group(1);
            String name = decodeString(matcher.group(2));
            String lastName = decodeString(matcher.group(3));
            String clienteDe = decodeString(matcher.group(4));
            String phone = decodeString(matcher.group(5));
            accountList.add(new Account(id, name, lastName, clienteDe, phone));
        }

        accountTable.setItems(accountList);
    }

    private void generarDocumento() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showError("Debes seleccionar una cuenta para generar los documentos.");
            return;
        }

        // Obtener el ID de la cuenta seleccionada
        String accountId = selectedAccount.getId();

        // Crear una instancia de OpenUrlExampleFX con el ID de la cuenta
        OpenUrlExampleFX app = new OpenUrlExampleFX(accountId);

        // Crear un nuevo Stage para la aplicación
        Stage stage = new Stage();

        // Llamar al método start de la aplicación
        try {
            app.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                return response.toString();
            }
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

    private void executePatchRequest(String url, String data) throws IOException {
        String bearerToken = "00DUB000001QzdZ!AQEAQHt6T.S.CnVpDb0wsACKEacY7O2Dvl8wL_I1S5KhP.LfjTxcTNmxvMx2snj5JXFK1CkUv7kNG.kd1j9BICF.utVQZ3E2";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.addHeader("Content-Type", "application/json");
        httpPatch.addHeader("Authorization", "Bearer " + bearerToken);

        // Configurar el cuerpo de la solicitud
        StringEntity entity = new StringEntity(data);
        httpPatch.setEntity(entity);

        // Ejecutar la solicitud y obtener la respuesta
        HttpResponse response = httpClient.execute(httpPatch);
        int statusCode = response.getStatusLine().getStatusCode();

        // Verificar el código de estado de la respuesta
        if (statusCode == 200 || statusCode == 204) {
            // La operación PATCH se realizó correctamente
            // Aquí puedes manejar la respuesta si es necesario
        } else {
            throw new IOException("Error al actualizar la cuenta. Código de respuesta HTTP: " + statusCode);
        }
    }

    public static void executePostRequest(String url, String data) throws IOException {
        String bearerToken = "00DUB000001QzdZ!AQEAQHt6T.S.CnVpDb0wsACKEacY7O2Dvl8wL_I1S5KhP.LfjTxcTNmxvMx2snj5JXFK1CkUv7kNG.kd1j9BICF.utVQZ3E2";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + bearerToken);

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

    private void crearCuentaNueva() {
        // Crear los campos de entrada para los datos del formulario de creación
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<String> clienteDeComboBox = new ComboBox<>();
        ComboBox<String> genderComboBox = new ComboBox<>(); // Cambiar a ComboBox
    
        // Agregar opciones al ComboBox de Cliente de
        clienteDeComboBox.getItems().addAll("LSO", "Programa");
    
        genderComboBox.getItems().addAll("Don", "Do\u00F1a");
    
    
        // Asignar el valor del Record Type
        String recordTypeId = "0127Q000000yrwrQAA";
    
        // Crear un botón "Crear"
        Button crearButton = new Button("Crear");
        crearButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        crearButton.setOnAction(event -> {
            // Obtener los valores de los campos del formulario de creación
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String clienteDe = clienteDeComboBox.getValue();
            String gender = genderComboBox.getValue(); // Obtener el valor seleccionado del ComboBox
    
            try {
                // Construir la URL del endpoint de Salesforce para crear una nueva cuenta
                String createUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/";
    
                String data = "{\"FirstName\": \"" + firstName + "\", " +
                        "\"LastName\": \"" + lastName + "\", " +
                        "\"PersonEmail\": \"" + email + "\", " +
                        "\"RecordTypeId\": \"" + recordTypeId + "\", " +
                        "\"Phone\": \"" + phone + "\", " +
                        "\"Cliente_de__c\": \"" + clienteDe + "\", " +
                        "\"Gender__pc\": \"" + gender.replace("ñ", "\\u00F1") + "\"}";
    
    
                // Ejecutar la solicitud POST a Salesforce para crear la nueva cuenta
                SalesforceOAuth.executePostRequest(createUrl, data);
    
                // Actualizar la tabla para reflejar los cambios
                executeAndDisplayResults();
    
                // Cerrar la ventana de creación después de crear la nueva cuenta en Salesforce
                Stage stage = (Stage) crearButton.getScene().getWindow();
                stage.close();
    
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error al crear la cuenta: " + e.getMessage());
            }
        });
    
        // Crear el diseño del formulario de creación
        VBox createRoot = new VBox();
        createRoot.setPadding(new Insets(20));
        createRoot.setSpacing(20);
        createRoot.getChildren().addAll(
                new Label("Nombre:"),
                firstNameField,
                new Label("Apellido:"),
                lastNameField,
                new Label("Email:"),
                emailField,
                new Label("Teléfono:"),
                phoneField,
                new Label("Cliente de:"),
                clienteDeComboBox,
                new Label("Género:"), // Cambiar a ComboBox
                genderComboBox
        );
    
        // Crear un contenedor para el botón "Crear"
        HBox buttonContainer = new HBox(crearButton);
        buttonContainer.setAlignment(Pos.CENTER);
    
        // Agregar el botón "Crear" al diseño del formulario de creación
        createRoot.getChildren().add(buttonContainer);
    
        // Configurar la escena y mostrar la ventana de creación
        Scene createScene = new Scene(createRoot, 400, 580);
        Stage createStage = new Stage();
        createStage.setScene(createScene);
        createStage.setTitle("Crear nueva cuenta");
        createStage.show();
    }
    
    

    private void editar() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            showError("Debes seleccionar una cuenta para editar.");
            return;
        }

        // Abrir una nueva ventana para el formulario de edición
        Stage editStage = new Stage();
        VBox editRoot = new VBox();
        editRoot.setPadding(new Insets(20));
        editRoot.setSpacing(20);
        TextField newFirstNameField = new TextField(selectedAccount.getName());
        TextField newLastNameField = new TextField(selectedAccount.getLastName());
        TextField newPhoneField = new TextField(selectedAccount.getPhone()); // Nuevo campo para el número de teléfono
        Button saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        saveButton.setOnAction(event -> {
            String newFirstName = newFirstNameField.getText();
            String newLastName = newLastNameField.getText();
            String newPhone = newPhoneField.getText(); // Obtener el nuevo número de teléfono
            selectedAccount.setName(newFirstName);
            selectedAccount.setLastName(newLastName);
            selectedAccount.setPhone(newPhone); // Actualizar el número de teléfono en el objeto Account
            try {
                // Construir la URL del endpoint de Salesforce para la cuenta específica
                String accountId = selectedAccount.getId();
                String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/" + accountId;

                // Construir los datos a enviar en la solicitud PATCH, incluyendo el número de teléfono
                String data = "{\"FirstName\": \"" + newFirstName + "\", \"LastName\": \"" + newLastName + "\", \"Phone\": \"" + newPhone + "\"}";

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

        HBox buttonContainer = new HBox(saveButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Agregar el nuevo campo de número de teléfono al diseño de la ventana de edición
        editRoot.getChildren().addAll(
                new Label("Nombre:"),
                newFirstNameField,
                new Label("Apellido:"),
                newLastNameField,
                new Label("Teléfono:"),
                newPhoneField, // Agregar el campo de número de teléfono al diseño
                buttonContainer // Agregar el contenedor que contiene el botón centrado
        );

        Scene editScene = new Scene(editRoot, 400, 350); // Aumentar la altura para incluir el nuevo campo
        editStage.setScene(editScene);
        editStage.setMinWidth(400);
        editStage.setMinHeight(350); // Ajustar la altura mínima para acomodar el nuevo campo
        editStage.setTitle("Editar Cuenta");
        editStage.show();
    }

    private void cerrarSesion() {
        // Aquí podrías agregar la lógica para cerrar la sesión actual
        System.out.println("Cerrando sesión...");
        // Por ahora, simplemente cierra la aplicación
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Account {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty clienteDe;
        private final SimpleStringProperty phone;

        public Account(String id, String name, String lastName, String clienteDe, String phone) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.lastName = new SimpleStringProperty(lastName);
            this.clienteDe = new SimpleStringProperty(clienteDe);
            this.phone = new SimpleStringProperty(phone);
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

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public String getCliente() {
            return clienteDe.get();
        }

        public void setCliente(String clienteDe) {
            this.clienteDe.set(clienteDe);
        }

        public String getPhone() {
            return phone.get();
        }

        public void setPhone(String phone) {
            this.phone.set(phone);
        }

        public SimpleStringProperty idProperty() {
            return id;
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }

        public SimpleStringProperty clienteDeProperty() {
            return clienteDe;
        }

        public SimpleStringProperty phoneProperty() {
            return phone;
        }
    }
}
