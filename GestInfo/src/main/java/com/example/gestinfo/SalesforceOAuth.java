package com.example.gestinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SalesforceOAuth extends Application {

    private TableView<Account> accountTable;
    private static Stage stage; // Modificado para que sea estático

    @SuppressWarnings("exports")
    public static Stage getStage() {
        return stage;
    }

    @SuppressWarnings({ "unchecked", "exports" })
    @Override
    public void start(Stage primaryStage) {
        // Configurar el layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
    
        // Crear el menú
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
    
        // Menú "Usuarios" con submenú "Ver usuarios"
        Menu usuariosMenu = new Menu("Usuarios");
        MenuItem verUsuariosItem = new MenuItem("Ver usuarios");
        verUsuariosItem.setOnAction(event -> {
            Stage stage = new Stage();
            ShowUsers showUsers = new ShowUsers();
            try {
                showUsers.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        usuariosMenu.getItems().add(verUsuariosItem);
    
        // Agregar los menús al MenuBar
        menuBar.getMenus().addAll(salirMenu, nuevoMenu, usuariosMenu);
    
        // Configurar la parte superior del BorderPane con el menú
        root.setTop(menuBar);

        // Crear el campo de búsqueda
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterAccounts(newValue));

        // Crear el botón de restablecer
        Button resetButton = new Button("Restablecer");
        resetButton.setOnAction(event -> {
            // Limpiar el campo de búsqueda y restablecer la tabla
            searchField.clear();
            executeAndDisplayResults();
        });

        // Crear el contenedor para el campo de búsqueda y el botón de restablecer
        // Crear el contenedor para el campo de búsqueda y el botón de restablecer
        HBox searchContainer = new HBox(10, searchField, resetButton);
        searchContainer.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setPadding(new Insets(0, 10, 10, 10)); // Establecer un margen izquierdo y derecho de 10 píxeles, y un margen inferior de 10 píxeles

        // Agregar margen inferior al searchContainer
        BorderPane.setMargin(searchContainer, new Insets(0, 0, 10, 0)); // Establecer un margen inferior de 10 píxeles


        // Configurar la parte superior del BorderPane con el menú y el campo de búsqueda
        VBox topContainer = new VBox(menuBar, searchContainer);
        topContainer.setSpacing(10);
        topContainer.setAlignment(Pos.CENTER);
        root.setTop(topContainer);

    
        // Crear la tabla para mostrar los datos de las cuentas
        accountTable = new TableView<>();
        accountTable.setStyle("-fx-background-color: white;");
    
        TableColumn<Account, String> nameColumn = new TableColumn<>("Nombre");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    
        TableColumn<Account, String> lastNameColumn = new TableColumn<>("Apellidos");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
    
        TableColumn<Account, String> clienteDeColumn = new TableColumn<>("Cliente de");
        clienteDeColumn.setCellValueFactory(cellData -> cellData.getValue().clienteDeProperty());
    
        TableColumn<Account, String> phoneColumn = new TableColumn<>("Teléfono");
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
    
        accountTable.getColumns().addAll(nameColumn, lastNameColumn, clienteDeColumn, phoneColumn);
    
        // Configurar los botones en la parte inferior
        Button generarDoc = new Button("Generar Documentos");
        generarDoc.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        generarDoc.setOnAction(event -> generarDocumento());
    
        Button editarButton = new Button("Editar");
        editarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        editarButton.setOnAction(event -> editar());
    
        HBox buttonBox = new HBox(generarDoc, editarButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
    
        // Agregar la tabla y los botones al layout principal
        root.setCenter(accountTable);
        root.setBottom(buttonBox);
    
        // Configurar la escena y mostrar la ventana
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("GestInfo");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));
        primaryStage.setMaximized(true); // Maximizar la ventana
        primaryStage.show();
    
        executeAndDisplayResults();
    }
    
    

    private String decodeString(String encodedString) {
        try {
            return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodedString;
        }
    }

    private void filterAccounts(String searchQuery) {
        // Obtener la lista de cuentas actual de la tabla
        ObservableList<Account> accounts = accountTable.getItems();
        
        // Crear una nueva lista para almacenar las cuentas filtradas
        ObservableList<Account> filteredAccounts = FXCollections.observableArrayList();
        
        // Recorrer todas las cuentas y agregar aquellas cuyo nombre coincida con la búsqueda
        for (Account account : accounts) {
            if (account.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredAccounts.add(account);
            }
        }
        
        // Actualizar la tabla con las cuentas filtradas
        accountTable.setItems(filteredAccounts);
    }
    

    private void executeAndDisplayResults() {
        try {
            // URL de la consulta
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,Cliente_de__c,Phone+FROM+Account+WHERE+Id+!=+null";
    
            // Token de portador
            String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
    
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
        } catch (IOException e) {
            mostrarMensajeError("No se puede conectar a Salesforce.");
        }
    }

    private void mostrarMensajeError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
        String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
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
        String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
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
        ComboBox<String> recordTypeComboBox = new ComboBox<>(); // ComboBox para RecordType
        
        // Agregar opciones al ComboBox de Cliente de
        clienteDeComboBox.getItems().addAll("LSO", "Programa");
        
        // Agregar opciones al ComboBox de Género
        genderComboBox.getItems().addAll("Don", "Do\u00F1a");
        
        // Agregar opciones al ComboBox de RecordType
        recordTypeComboBox.getItems().addAll("Cliente potencial", "Cliente");
        
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
            String recordType = recordTypeComboBox.getValue(); // Obtener el valor seleccionado del ComboBox
        
            // Verificar que se haya seleccionado un RecordType
            if (recordType == null || recordType.isEmpty()) {
                showError("Por favor, seleccione un Record Type.");
                return;
            }
        
            try {
                // Construir la URL del endpoint de Salesforce para crear una nueva cuenta
                String createUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/";
        
                String data = "{\"FirstName\": \"" + firstName + "\", " +
                        "\"LastName\": \"" + lastName + "\", " +
                        "\"PersonEmail\": \"" + email + "\", " +
                        "\"RecordTypeId\": \"" + getRecordTypeId(recordType) + "\", " +
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
                genderComboBox,
                new Label("Record Type:"),
                recordTypeComboBox
        );
        
        // Crear un contenedor para el botón "Crear"
        HBox buttonContainer = new HBox(crearButton);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Agregar el botón "Crear" al diseño del formulario de creación
        createRoot.getChildren().add(buttonContainer);
        
        // Configurar la escena y mostrar la ventana de creación
        Scene createScene = new Scene(createRoot, 400, 640);
        Stage createStage = new Stage();
        createStage.setScene(createScene);
        createStage.setTitle("Crear nueva cuenta");
        createStage.show();
    }
    
    // Método para obtener el ID del RecordType basado en el nombre seleccionado
    private String getRecordTypeId(String recordTypeName) {
        if (recordTypeName.equals("Cliente potencial")) {
            return "0127Q000000yrwrQAA";
        } else if (recordTypeName.equals("Cliente")) {
            return "0127Q000000yrwqQAA";
        }
        return null; // Manejar otros casos según sea necesario
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
        ComboBox<String> newClienteDeComboBox = new ComboBox<>();
        newClienteDeComboBox.getItems().addAll("LSO", "Programa");
        newClienteDeComboBox.setValue(selectedAccount.getCliente()); // Establecer el valor predeterminado del ComboBox

        Button saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        saveButton.setOnAction(event -> {
            String newFirstName = newFirstNameField.getText();
            String newLastName = newLastNameField.getText();
            String newPhone = newPhoneField.getText(); // Obtener el nuevo número de teléfono
            String newClienteDe = newClienteDeComboBox.getValue(); // Obtener el nuevo valor del ComboBox

            selectedAccount.setName(newFirstName);
            selectedAccount.setLastName(newLastName);
            selectedAccount.setPhone(newPhone); // Actualizar el número de teléfono en el objeto Account
            selectedAccount.setCliente(newClienteDe); // Actualizar el cliente en el objeto Account

            try {
                // Construir la URL del endpoint de Salesforce para la cuenta específica
                String accountId = selectedAccount.getId();
                String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/" + accountId;

                // Construir los datos a enviar en la solicitud PATCH, incluyendo el número de teléfono y el cliente
                String data = "{\"FirstName\": \"" + newFirstName + "\", \"LastName\": \"" + newLastName + "\", \"Phone\": \"" + newPhone + "\", \"Cliente_de__c\": \"" + newClienteDe + "\"}";

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
                new Label("Cliente de:"),
                newClienteDeComboBox, // Agregar el ComboBox para el cliente
                buttonContainer // Agregar el contenedor que contiene el botón centrado
        );

        Scene editScene = new Scene(editRoot, 400, 400); // Aumentar la altura para incluir el nuevo campo
        editStage.setScene(editScene);
        editStage.setMinWidth(400);
        editStage.setMinHeight(400); // Ajustar la altura mínima para acomodar el nuevo campo
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

        @SuppressWarnings("exports")
        public SimpleStringProperty idProperty() {
            return id;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty nameProperty() {
            return name;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty clienteDeProperty() {
            return clienteDe;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty phoneProperty() {
            return phone;
        }
    }
}