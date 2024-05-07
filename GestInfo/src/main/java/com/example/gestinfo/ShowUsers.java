package com.example.gestinfo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ShowUsers extends Application {

    private static TableView<User> userTable;
    private static TextField searchField;

    private static ObservableList<User> originalUserList; // Almacena la lista original de usuarios

    @SuppressWarnings("unchecked")
    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        // Configurar el layout principal
        VBox root = new VBox();
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #f0f0f0;");

        MenuBar menuBar = new MenuBar();

        // Menú "Salir"
        Menu salirMenu = new Menu("Salir");
        MenuItem salirProgramaItem = new MenuItem("Salir del programa");
        salirProgramaItem.setOnAction(event -> {
            // Lógica para salir del programa
            System.exit(0);
        });
        salirMenu.getItems().add(salirProgramaItem);

        // Menú "Volver"
        Menu volverMenu = new Menu("Volver");
        MenuItem volverCuentasItem = new MenuItem("Volver a las cuentas");
        volverCuentasItem.setOnAction(event -> {
            // Cerrar la ventana actual de ShowUsers
            primaryStage.close();
        });
        volverMenu.getItems().add(volverCuentasItem);

        // Agregar los menús al MenuBar
        menuBar.getMenus().addAll(salirMenu, volverMenu);

        // Crear el campo de búsqueda
        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));

        // Crear el botón de restablecer
        Button resetButton = new Button("Restablecer");
        resetButton.setOnAction(event -> {
            // Limpiar el campo de búsqueda y restablecer la tabla
            searchField.clear();
            userTable.setItems(originalUserList); // Restablecer la tabla con la lista original de usuarios
        });

        // Crear el contenedor para el campo de búsqueda y el botón de restablecer
        HBox searchContainer = new HBox(10, searchField, resetButton);
        searchContainer.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setPadding(new Insets(0, 10, 0, 10)); // Establecer un margen izquierdo y derecho de 10 píxeles



        // Crear la tabla para mostrar los IDs y los Names
        userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: white;");

        TableColumn<User, String> firstNameColumn = new TableColumn<>("Nombre");
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

        TableColumn<User, String> lastNameColumn = new TableColumn<>("Apellidos");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        TableColumn<User, String> userRoleIdColumn = new TableColumn<>("Rol de usuario");
        userRoleIdColumn.setCellValueFactory(cellData -> cellData.getValue().userRoleIdProperty());

        userTable.getColumns().addAll(firstNameColumn, lastNameColumn, userRoleIdColumn);

        executeAndDisplayResults(primaryStage);

        // Establecer la prioridad de crecimiento vertical de la tabla
        VBox.setVgrow(userTable, Priority.ALWAYS);

        // Crear el botón de editar
        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");

        // Configurar el evento del botón de editar
        editButton.setOnAction(event -> {
            // Obtener el usuario seleccionado
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Mostrar una ventana de diálogo para la edición del usuario
                Stage editStage = new Stage();
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.setTitle("Editar Usuario");
        
                VBox editRoot = new VBox(10);
                editRoot.setAlignment(Pos.CENTER);
                editRoot.setPadding(new Insets(20));
        
                // Campos para editar el nombre y apellidos del usuario
                TextField firstNameField = new TextField(selectedUser.getFirstName());
                TextField lastNameField = new TextField(selectedUser.getLastName());
        
                Button saveButton = new Button("Guardar");
                saveButton.setOnAction(saveEvent -> {
                    // Actualizar el usuario con los nuevos valores
                    selectedUser.setFirstName(firstNameField.getText());
                    selectedUser.setLastName(lastNameField.getText());
                    // Actualizar el usuario en Salesforce
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"FirstName\": \"" + selectedUser.getFirstName() + "\", \"LastName\": \"" + selectedUser.getLastName() + "\"}";
                        executePatchRequest(updateUrl, requestBody);
                        // Actualizar la tabla para reflejar los cambios
                        userTable.refresh();
                        // Cerrar la ventana de edición
                        editStage.close();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", editStage);
                    }
                });
        
                editRoot.getChildren().addAll(new Label("Nombre:"), firstNameField, new Label("Apellidos:"), lastNameField, saveButton);
        
                Scene editScene = new Scene(editRoot, 300, 200);
                editStage.setScene(editScene);
                editStage.showAndWait();
            } else {
                // Mostrar un mensaje de error si no se selecciona ningún usuario
                mostrarMensajeError("Por favor, selecciona un usuario para editar.", primaryStage);
            }
        });
        


        // Crear un VBox para centrar verticalmente el botón debajo de la tabla
        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().add(editButton);

        // Establecer el margen inferior del VBox
        VBox.setMargin(buttonContainer, new Insets(0, 0, 10, 0)); // 10 píxeles de margen inferior

        // Agregar el VBox que contiene el botón debajo de la tabla
        root.getChildren().addAll(menuBar, searchContainer, userTable, buttonContainer);

        // Configurar la escena
        Scene scene = new Scene(root);

        // Maximizar la ventana
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        // Mostrar la ventana maximizada
        primaryStage.setScene(scene);
        primaryStage.setTitle("GestInfo");
        primaryStage.show();

        // Cerrar la ventana de SalesforceOAuth si está abierta
        Stage salesforceOAuthStage = SalesforceOAuth.getStage();
        if (salesforceOAuthStage != null) {
            salesforceOAuthStage.close();
        }
    }

    private void executePatchRequest(String url, String data) throws IOException {
        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQK6CEZVdhaGEAyONyl5LYc6xruyzuh6obdEss0FcE6xXZMX01TNOUNZW_wG94s0MPv9HwSGw6s2oyE1ogH7NtpmuWYsZ";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.addHeader("Content-Type", "application/json");
        httpPatch.addHeader("Authorization", "Bearer " + bearerToken);

        // Configurar el cuerpo de la solicitud
        StringEntity entity = new StringEntity(data);
        httpPatch.setEntity(entity);

        // Ejecutar la solicitud y obtener la respuesta
        CloseableHttpResponse response = httpClient.execute(httpPatch);
        int statusCode = response.getStatusLine().getStatusCode();

        // Verificar el código de estado de la respuesta
        if (statusCode == 200 || statusCode == 204) {
            // La operación PATCH se realizó correctamente
            // Aquí puedes manejar la respuesta si es necesario
        } else {
            throw new IOException("Error al actualizar el usuario. Código de respuesta HTTP: " + statusCode);
        }
    }


    private static String decodeString(String encodedString) {
        try {
            return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return encodedString;
        }
    }

    private static String executeQuery(String url, String bearerToken) throws IOException {
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

    private static void executeAndDisplayResults(Stage primaryStage) {
        try {
            // URL de la consulta
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,UserRoleId,ProfileId,CompanyName,Username,Email,Alias,TimeZoneSidKey,LocaleSidKey,EmailEncodingKey,LanguageLocaleKey+FROM+User+WHERE+UserRoleId+!=+null+AND+IsActive+=+true+AND+ProfileId+!=+null";
    
            // Token de portador
            String bearerToken = "00DUB000001QzdZ!AQEAQK6CEZVdhaGEAyONyl5LYc6xruyzuh6obdEss0FcE6xXZMX01TNOUNZW_wG94s0MPv9HwSGw6s2oyE1ogH7NtpmuWYsZ";
    
            // Realizar la consulta
            String response = executeQuery(queryUrl, bearerToken);
    
            // Procesar la respuesta y mostrar los IDs y los Names en la tabla
            ObservableList<User> userList = FXCollections.observableArrayList();
    
            Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"FirstName\"\\s*:\\s*\"(.*?)\",\"LastName\"\\s*:\\s*\"(.*?)\",\"UserRoleId\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String id = matcher.group(1);
                String firstName = decodeString(matcher.group(2));
                String lastName = decodeString(matcher.group(3));
                String userRoleId = decodeString(matcher.group(4));
                userList.add(new User(id, firstName, lastName, userRoleId));
            }
    
            userTable.setItems(userList);
            originalUserList = userList; // Guardar la lista original de usuarios
        } catch (IOException e){
            mostrarMensajeError("No se puede conectar a Salesforce.", primaryStage);
        }
    }

    private static void mostrarMensajeError(String mensaje, Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    

    private static void filterUsers(String searchText) {
        // Obtener la lista de usuarios original
        ObservableList<User> userList = originalUserList;

        // Si el texto de búsqueda está vacío, mostrar todos los usuarios nuevamente
        if (searchText == null || searchText.isEmpty()) {
            userTable.setItems(userList);
            return;
        }

        // Crear una nueva lista para almacenar los usuarios filtrados
        ObservableList<User> filteredList = FXCollections.observableArrayList();

        // Iterar sobre la lista de usuarios y agregar aquellos que coincidan con el criterio de búsqueda
        for (User user : userList) {
            if (user.getFirstName().toLowerCase().contains(searchText.toLowerCase()) ||
                user.getLastName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(user);
            }
        }

        // Establecer la lista filtrada como la nueva lista de elementos de la tabla
        userTable.setItems(filteredList);
    }

    public static class User {
        private final SimpleStringProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty userRoleId;

        public User(String id, String firstName, String lastName, String userRoleId) {
            this.id = new SimpleStringProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.userRoleId = new SimpleStringProperty(userRoleId);
        }

        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public String getUserRoleId() {
            return userRoleId.get();
        }

        public void setUserRoleId(String userRoleId) {
            this.userRoleId.set(userRoleId);
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty idProperty() {
            return id;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty firstNameProperty() {
            return firstName;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }

        @SuppressWarnings("exports")
        public SimpleStringProperty userRoleIdProperty() {
            return userRoleId;
        }
    }
}
