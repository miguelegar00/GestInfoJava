package com.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
import com.example.gestinfo.SalesforceOAuth;

public class ShowUsers extends Application {

    private static TableView<User> userTable;
    private static TextField searchField;

    private static ObservableList<User> originalUserList; // Almacena la lista original de usuarios

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {
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

        // Realizar la consulta y mostrar los resultados en la tabla
        try {
            executeAndDisplayResults();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al realizar la consulta: " + e.getMessage());
        }

        // Establecer la prioridad de crecimiento vertical de la tabla
        VBox.setVgrow(userTable, Priority.ALWAYS);

        // Agregar el menú, el campo de búsqueda y la tabla al layout principal
        root.getChildren().addAll(menuBar, searchContainer, userTable);

        // Configurar la escena
        Scene scene = new Scene(root);

        // Maximizar la ventana
        primaryStage.setMaximized(true);

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

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private static void executeAndDisplayResults() throws IOException {
        // URL de la consulta
        String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,UserRoleId,ProfileId+FROM+User+WHERE+UserRoleId+!=+null+AND+IsActive+=+true+AND+ProfileId+!=+null";

        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQBMCu7oCBBM_JhGnf2o2VpesC9PkuU1742rf2KtV9dHTDDAcmGTv3C3bcRDlrEe9hEhVQ49GghG3djc3R8e2grQkhbQA";

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

        public SimpleStringProperty idProperty() {
            return id;
        }

        public SimpleStringProperty firstNameProperty() {
            return firstName;
        }

        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }

        public SimpleStringProperty userRoleIdProperty() {
            return userRoleId;
        }
    }
}
