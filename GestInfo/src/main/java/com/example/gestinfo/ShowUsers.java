package com.example.gestinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ShowUsers extends Application {

    ShowCases showCases = new ShowCases();
    
    private static SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    private static TableView<User> userTable;
    private static TextField searchField;

    private static ObservableList<User> originalUserList;

    private static final Map<String, String> roleNames = new HashMap<>();

    static {
        try {
            // Cargar los roles desde el txt
            loadRoleNames("/com/example/gestinfo/roles.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading role names: " + e.getMessage());
        }
    }

    private static void loadRoleNames(String filename) throws IOException {
        InputStream inputStream = ShowUsers.class.getResourceAsStream(filename);
        if (inputStream == null) {
            System.err.println("File not found: " + filename);
            return;
        }
    
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    roleNames.put(parts[0], decodeString(parts[1]));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        
        VBox root = new VBox();
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #f0f0f0;");

        MenuBar menuBar = new MenuBar();

        Menu salirMenu = new Menu("Salir");
        MenuItem salirProgramaItem = new MenuItem("Salir del programa");
        salirProgramaItem.setOnAction(event -> {
            
            System.exit(0);
        });
        salirMenu.getItems().add(salirProgramaItem);

        
        Menu volverMenu = new Menu("Volver");
        MenuItem volverCuentasItem = new MenuItem("Volver a las cuentas");
        volverCuentasItem.setOnAction(event -> {
            
            primaryStage.close();
        });
        volverMenu.getItems().add(volverCuentasItem);

        menuBar.getMenus().addAll(salirMenu, volverMenu);

        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));

        Button resetButton = new Button("Restablecer");
        resetButton.setOnAction(event -> {
            // Limpiar el campo de búsqueda y restablecer la tabla
            searchField.clear();
            userTable.setItems(originalUserList); // Restablece la tabla con los datos obtenidos de nuevo en la query
        });

        HBox searchContainer = new HBox(10, searchField, resetButton);
        searchContainer.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setPadding(new Insets(0, 10, 0, 10));



        // Crear la tabla para mostrar los IDs y los nombres de los usuario
        userTable = new TableView<>();
        userTable.setStyle("-fx-background-color: white;");

        TableColumn<User, String> firstNameColumn = new TableColumn<>("Nombre");
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

        TableColumn<User, String> lastNameColumn = new TableColumn<>("Apellidos");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        TableColumn<User, String> userRoleIdColumn = new TableColumn<>("Rol de usuario");
        userRoleIdColumn.setCellValueFactory(cellData -> {
            String roleId = cellData.getValue().getUserRoleId();
            // Verificar si el ID del rol está en el mapa
            if (roleNames.containsKey(roleId)) {
                
                return new SimpleStringProperty(roleNames.get(roleId));
            } else {
                
                return new SimpleStringProperty(roleId);
            }
        });

        TableColumn<User, String> isActiveColumn = new TableColumn<>("¿Usuario activo?");
        isActiveColumn.setCellValueFactory(cellData -> {
            Boolean isActive = cellData.getValue().getIsActive();
            return new SimpleStringProperty(isActive ? "Sí" : "No");
        });

        userTable.getColumns().addAll(firstNameColumn, lastNameColumn, userRoleIdColumn, isActiveColumn);

        executeAndDisplayResults(primaryStage);

        VBox.setVgrow(userTable, Priority.ALWAYS);

        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");

        editButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Stage editStage = new Stage();
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.setTitle("Editar Usuario");
    
                VBox editRoot = new VBox(10);
                editRoot.setAlignment(Pos.CENTER);
                editRoot.setPadding(new Insets(20));
    
                TextField firstNameField = new TextField(selectedUser.getFirstName());
                TextField lastNameField = new TextField(selectedUser.getLastName());
    
                ComboBox<String> roleIdComboBox = new ComboBox<>();
                roleIdComboBox.setPromptText("Seleccionar rol");
    
                roleIdComboBox.getItems().addAll(roleNames.values());
                roleIdComboBox.setValue(roleNames.get(selectedUser.getUserRoleId()));
    
                Button saveButton = new Button("Guardar");
                saveButton.setOnAction(saveEvent -> {
                    selectedUser.setFirstName(firstNameField.getText());
                    selectedUser.setLastName(lastNameField.getText());
                    selectedUser.setUserRoleId(getKeyFromValue(roleNames, roleIdComboBox.getValue()));

                    // Actualizar el usuario que hayamos elegido en Salesforce y en la tabla de mi programa
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"FirstName\": \"" + selectedUser.getFirstName() + "\", \"LastName\": \"" + selectedUser.getLastName() + "\", \"UserRoleId\": \"" + selectedUser.getUserRoleId() + "\"}";
                        executePatchRequest(updateUrl, requestBody);

                        // Actualizar la tabla para reflejar los cambios
                        userTable.refresh();
                        
                        editStage.close();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", editStage);
                    }
                });
        
                editRoot.getChildren().addAll(new Label("Nombre:"), firstNameField, new Label("Apellidos:"), lastNameField, new Label("Rol de usuario:"), roleIdComboBox, saveButton);
        
                Scene editScene = new Scene(editRoot, 300, 250);
                editStage.setScene(editScene);
                editStage.showAndWait();
            } else {
                
                mostrarMensajeError("Por favor, selecciona un usuario para editar.", primaryStage);
            }
        });

        
        Button activateButton = new Button("Activar");
        activateButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");

        
        activateButton.setOnAction(event -> {
            // Obtengo el usuario que hayamos seleccionado
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {

                //Mensaje para confirmar que queremos activar este usuario
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de activación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres activar a este usuario?");

                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {

                    selectedUser.setIsActive(true);
                    
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": true}";
                        executePatchRequest(updateUrl, requestBody);
                        userTable.refresh();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", primaryStage);
                    }
                }
            } else {
                mostrarMensajeError("Por favor, selecciona un usuario para activar.", primaryStage);
            }
        });

        Button desactivateButton = new Button("Desactivar");
        desactivateButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        desactivateButton.setOnAction(event -> {
            
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de desactivación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres desactivar a este usuario?");

                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    
                    selectedUser.setIsActive(false);
                    
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": false}";
                        executePatchRequest(updateUrl, requestBody);
                        
                        userTable.refresh();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", primaryStage);
                    }
                }
            } else {
                
                mostrarMensajeError("Por favor, selecciona un usuario para desactivar.", primaryStage);
            }
        });

        Button caseButton = new Button("Ver casos");
        caseButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");

        caseButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    
                    String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Subject,Status,Id+FROM+Case+WHERE+OwnerId='" + selectedUser.getId() + "'";
                    String bearerToken = tokenManager.getNewAccessToken();
                    String response = executeQuery(queryUrl, bearerToken);

                    if (response.contains("\"totalSize\":0")) {
                        
                        mostrarMensajeError("El usuario seleccionado no tiene ningún caso asignado.", primaryStage);
                    } else {
                        
                        showCases.abrirVentanaCasos(selectedUser);
                    }
                } catch (IOException e) {
                    mostrarMensajeError("Error al verificar los casos del usuario.", primaryStage);
                }
            } else {
                mostrarMensajeError("Por favor, selecciona un usuario para ver los casos.", primaryStage);
            }
        });
        
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10); // Espacio entre los botones
        buttonContainer.getChildren().addAll(activateButton, editButton, desactivateButton, caseButton);

        VBox.setMargin(buttonContainer, new Insets(0, 0, 10, 0));

        root.getChildren().addAll(menuBar, searchContainer, userTable, buttonContainer);

        Scene scene = new Scene(root);

        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        primaryStage.setScene(scene);
        primaryStage.setTitle("GestInfo");
        primaryStage.show();

        Stage salesforceOAuthStage = SalesforceOAuth.getStage();
        if (salesforceOAuthStage != null) {
            salesforceOAuthStage.close();
        }
    }
    
    private <K, V> K getKeyFromValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void executePatchRequest(String url, String data) throws IOException {
        
        String bearerToken = tokenManager.getNewAccessToken();

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.addHeader("Content-Type", "application/json");
        httpPatch.addHeader("Authorization", "Bearer " + bearerToken);

        StringEntity entity = new StringEntity(data);
        httpPatch.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPatch);
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200 || statusCode == 204) {
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

        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);

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
            
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,UserRoleId,IsActive+FROM+User+WHERE+UserRoleId+!=+null+AND+ProfileId+!=+null+AND+UserRoleId+!=+null";
    
            String bearerToken = tokenManager.getNewAccessToken();
    
            String response = executeQuery(queryUrl, bearerToken);
    
            ObservableList<User> userList = FXCollections.observableArrayList();
    
            Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\",\"FirstName\"\\s*:\\s*\"(.*?)\",\"LastName\"\\s*:\\s*\"(.*?)\",\"UserRoleId\"\\s*:\\s*\"(.*?)\",\"IsActive\"\\s*:\\s*(true|false)");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String id = matcher.group(1);
                String firstName = decodeString(matcher.group(2));
                String lastName = decodeString(matcher.group(3));
                String userRoleId = decodeString(matcher.group(4));
                Boolean isActive = Boolean.parseBoolean(matcher.group(5));
                userList.add(new User(id, firstName, lastName, userRoleId, isActive));
            }
    
            userTable.setItems(userList);
            originalUserList = userList;
        } catch (IOException e){
            mostrarMensajeError("No se puede conectar a Salesforce.", primaryStage);
        }
    }

    private static void mostrarMensajeError(String mensaje, Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void filterUsers(String searchText) {

        ObservableList<User> userList = originalUserList;

        if (searchText == null || searchText.isEmpty()) {
            userTable.setItems(userList);
            return;
        }

        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : userList) {
            if (user.getFirstName().toLowerCase().contains(searchText.toLowerCase()) ||
                user.getLastName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(user);
            }
        }

        userTable.setItems(filteredList);
    }

    public static class User {
        private final SimpleStringProperty id;
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty userRoleId;
        private boolean isActive;

        public User(String id, String firstName, String lastName, String userRoleId, Boolean isActive) {
            this.id = new SimpleStringProperty(id);
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.userRoleId = new SimpleStringProperty(userRoleId);
            this.isActive = isActive;
        }

        public String getId() {
            return id.get();
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
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