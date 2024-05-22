package com.example.gestinfo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class ShowUsers extends Application {

    private static TableView<User> userTable;
    private static TextField searchField;

    private static ObservableList<User> originalUserList;

    private static final Map<String, String> roleNames = new HashMap<>();

    static {
        try {
            // Cargar roles.txt desde resources
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
        userRoleIdColumn.setCellValueFactory(cellData -> {
            String roleId = cellData.getValue().getUserRoleId();
            // Verificar si el ID del rol está en el mapa
            if (roleNames.containsKey(roleId)) {
                // Si está en el mapa, devuelve el nombre del rol correspondiente
                return new SimpleStringProperty(roleNames.get(roleId));
            } else {
                // De lo contrario, devuelve el ID del rol
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

        // Establecer la prioridad de crecimiento vertical de la tabla
        VBox.setVgrow(userTable, Priority.ALWAYS);

        // Crear el botón de editar
        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");

        // Configurar el evento del botón de editar
        editButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Stage editStage = new Stage();
                editStage.initModality(Modality.APPLICATION_MODAL);
                editStage.setTitle("Editar Usuario");
    
                VBox editRoot = new VBox(10);
                editRoot.setAlignment(Pos.CENTER);
                editRoot.setPadding(new Insets(20));
    
                // Campos para editar el nombre y apellidos del usuario
                TextField firstNameField = new TextField(selectedUser.getFirstName());
                TextField lastNameField = new TextField(selectedUser.getLastName());
    
                // ComboBox para seleccionar el userRoleId
                ComboBox<String> roleIdComboBox = new ComboBox<>();
                roleIdComboBox.setPromptText("Seleccionar rol");
    
                // Agregar los roles disponibles al ComboBox
                roleIdComboBox.getItems().addAll(roleNames.values());
                roleIdComboBox.setValue(roleNames.get(selectedUser.getUserRoleId()));
    
                Button saveButton = new Button("Guardar");
                saveButton.setOnAction(saveEvent -> {
                    selectedUser.setFirstName(firstNameField.getText());
                    selectedUser.setLastName(lastNameField.getText());
                    // Actualizar el userRoleId con el valor seleccionado en el ComboBox
                    selectedUser.setUserRoleId(getKeyFromValue(roleNames, roleIdComboBox.getValue()));
                    // Actualizar el usuario en Salesforce
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"FirstName\": \"" + selectedUser.getFirstName() + "\", \"LastName\": \"" + selectedUser.getLastName() + "\", \"UserRoleId\": \"" + selectedUser.getUserRoleId() + "\"}";
                        executePatchRequest(updateUrl, requestBody);
                        // Actualizar la tabla para reflejar los cambios
                        userTable.refresh();
                        // Cerrar la ventana de edición
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
                // Mostrar un mensaje de error si no se selecciona ningún usuario
                mostrarMensajeError("Por favor, selecciona un usuario para editar.", primaryStage);
            }
        });

        // Crear el botón de borrar
        Button activateButton = new Button("Activar");
        activateButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");

        // Configurar el evento del botón de borrar
        activateButton.setOnAction(event -> {
            // Obtener el usuario seleccionado
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Mostrar un diálogo de confirmación antes de borrar
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de activación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres activar a este usuario?");

                // Obtener la respuesta del usuario desde el diálogo de confirmación
                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Cambiar el campo IsActive a null
                    selectedUser.setIsActive(true);
                    
                    // Actualizar el usuario en Salesforce
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": true}";
                        executePatchRequest(updateUrl, requestBody);
                        // Actualizar la tabla para reflejar los cambios
                        userTable.refresh();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", primaryStage);
                    }
                }
            } else {
                // Mostrar un mensaje de error si no se selecciona ningún usuario
                mostrarMensajeError("Por favor, selecciona un usuario para activar.", primaryStage);
            }
        });

        // Crear el botón de desactivar
        Button desactivateButton = new Button("Desactivar");
        desactivateButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // Configurar el evento del botón de desactivar
        desactivateButton.setOnAction(event -> {
            // Obtener el usuario seleccionado
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                // Mostrar un diálogo de confirmación antes de desactivar
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de desactivación");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres desactivar a este usuario?");

                // Obtener la respuesta del usuario desde el diálogo de confirmación
                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // Cambiar el campo IsActive a null
                    selectedUser.setIsActive(false);
                    
                    // Actualizar el usuario en Salesforce
                    try {
                        String updateUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/User/" + selectedUser.getId();
                        String requestBody = "{\"IsActive\": false}";
                        executePatchRequest(updateUrl, requestBody);
                        // Actualizar la tabla para reflejar los cambios
                        userTable.refresh();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el usuario en Salesforce.", primaryStage);
                    }
                }
            } else {
                // Mostrar un mensaje de error si no se selecciona ningún usuario
                mostrarMensajeError("Por favor, selecciona un usuario para desactivar.", primaryStage);
            }
        });

        Button caseButton = new Button("Ver casos");
        caseButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");

        // Configurar el evento del botón de ver casos
        caseButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                try {
                    // Realizar la consulta para verificar si hay casos del usuario seleccionado
                    String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Subject,Status,Id+FROM+Case+WHERE+OwnerId='" + selectedUser.getId() + "'";
                    String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
                    String response = executeQuery(queryUrl, bearerToken);

                    // Procesar la respuesta para verificar si hay casos
                    if (response.contains("\"totalSize\":0")) {
                        // No hay casos, mostrar un mensaje de error
                        mostrarMensajeError("El usuario seleccionado no tiene ningún caso asignado.", primaryStage);
                    } else {
                        // Hay casos, abrir la ventana de casos
                        abrirVentanaCasos(selectedUser);
                    }
                } catch (IOException e) {
                    mostrarMensajeError("Error al verificar los casos del usuario.", primaryStage);
                }
            } else {
                mostrarMensajeError("Por favor, selecciona un usuario para ver los casos.", primaryStage);
            }
        });

        // Crear un HBox para colocar los botones uno al lado del otro debajo de la tabla
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10); // Espacio entre los botones
        buttonContainer.getChildren().addAll(activateButton, editButton, desactivateButton, caseButton);



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
    
    @SuppressWarnings("unchecked")
    private void abrirVentanaCasos(User selectedUser) {
        Stage caseStage = new Stage();
        caseStage.initModality(Modality.APPLICATION_MODAL);
        caseStage.setTitle("Casos de " + selectedUser.getFirstName() + " " + selectedUser.getLastName());

        TableView<CaseInfo> caseTable = new TableView<>();
        TableColumn<CaseInfo, String> subjectColumn = new TableColumn<>("Asunto");
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

        TableColumn<CaseInfo, String> stageColumn = new TableColumn<>("Estado");
        stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));

        caseTable.getColumns().addAll(subjectColumn, stageColumn);

        try {
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,Subject,Status+FROM+Case+WHERE+OwnerId='" + selectedUser.getId() + "'";
            String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
            String response = executeQuery(queryUrl, bearerToken);

            ObservableList<CaseInfo> caseList = FXCollections.observableArrayList();
            Pattern pattern = Pattern.compile("\"Id\"\\s*:\\s*\"(\\w+)\".*?\"Subject\"\\s*:\\s*\"(.*?)\".*?\"Status\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                String id = matcher.group(1);
                String subject = decodeString(matcher.group(2));
                String stage = decodeString(matcher.group(3));
                caseList.add(new CaseInfo(id, subject, stage));
            }
            caseTable.setItems(caseList);
        } catch (IOException e) {
            mostrarMensajeError("Error al obtener los casos del usuario.", caseStage);
        }
        Button modifyStatusButton = new Button("Modificar Estado");
        modifyStatusButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        modifyStatusButton.setOnAction(event -> {
            CaseInfo selectedCase = caseTable.getSelectionModel().getSelectedItem();
            if (selectedCase != null) {
                System.out.println("Caso seleccionado: " + selectedCase.getCaseId() + " - " + selectedCase.getSubject());
                abrirModificarEstadoVentana(selectedCase);
            } else {
                mostrarMensajeError("Por favor, selecciona un caso para modificar su estado.", caseStage);
            }
        });


        VBox caseRoot = new VBox(10);
        caseRoot.setAlignment(Pos.CENTER);
        caseRoot.setPadding(new Insets(20));
        caseRoot.getChildren().addAll(caseTable, modifyStatusButton);

        Scene caseScene = new Scene(caseRoot, 410, 300);
        caseStage.setScene(caseScene);
        caseStage.showAndWait();
    }

    private void abrirModificarEstadoVentana(CaseInfo selectedCase) {
        Stage modifyStage = new Stage();
        modifyStage.initModality(Modality.APPLICATION_MODAL);
        modifyStage.setTitle("Modificar estado del caso");
    
        Label caseLabel = new Label("Asunto: " + selectedCase.getSubject());
        Label statusLabel = new Label("Estado actual: " + selectedCase.getStage());
    
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Esperando respuesta del cliente", "En Proceso", "Completado Ganado", "Completado Perdido", "Revisado", "Sin Tocar");
        statusComboBox.setValue(selectedCase.getStage());
    
        Button saveButton = new Button("Guardar");
        saveButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        saveButton.setOnAction(event -> {
            String newStatus = statusComboBox.getValue();
            if (newStatus != null && !newStatus.isEmpty()) {
                // Mostrar un diálogo de confirmación antes de guardar
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de cambio de estado");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres cambiar el estado del caso?");
    
                // Obtener la respuesta del usuario desde el diálogo de confirmación
                Optional<ButtonType> result = confirmationDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        actualizarEstadoCaso(selectedCase, newStatus);
                        selectedCase.setStage(newStatus);
                        modifyStage.close();
                    } catch (IOException e) {
                        mostrarMensajeError("Error al actualizar el estado del caso: " + e.getMessage(), modifyStage);
                    }
                }
            } else {
                mostrarMensajeError("Seleccione un estado válido.", modifyStage);
            }
        });
    
        VBox modifyRoot = new VBox(10);
        modifyRoot.setAlignment(Pos.CENTER);
        modifyRoot.setPadding(new Insets(20));
        modifyRoot.getChildren().addAll(caseLabel, statusLabel, statusComboBox, saveButton);
    
        Scene modifyScene = new Scene(modifyRoot, 300, 200);
        modifyStage.setScene(modifyScene);
        modifyStage.showAndWait();
    }
    
    private void actualizarEstadoCaso(CaseInfo selectedCase, String newStatus) throws IOException {
        String url = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Case/" + selectedCase.getCaseId();
        String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
        String data = "{\"Status\": \"" + newStatus + "\"}";
    
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
    
        // Manejar la respuesta
        if (statusCode == 200 || statusCode == 204) {
            // La operación PATCH se realizó correctamente
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"))) {
                StringBuilder responseContent = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseContent.append(responseLine.trim());
                }
                throw new IOException("Error al actualizar el estado del caso: " + responseContent.toString());
            }
        }
    }
    
    
    
    
    public static class CaseInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty subject;
        private final SimpleStringProperty stage;
    
        public CaseInfo(String id, String subject, String stage) {
            this.id = new SimpleStringProperty(id);
            this.subject = new SimpleStringProperty(subject);
            this.stage = new SimpleStringProperty(stage);
        }
    
        public String getCaseId() {
            return id.get();
        }
    
        public String getSubject() {
            return subject.get();
        }
    
        public SimpleStringProperty subjectProperty() {
            return subject;
        }
    
        public String getStage() {
            return stage.get();
        }
    
        public void setStage(String stage) {
            this.stage.set(stage);
        }
    
        public SimpleStringProperty stageProperty() {
            return stage;
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
        // Token de portador
        String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";

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
            String queryUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/query/?q=SELECT+Id,FirstName,LastName,UserRoleId,IsActive+FROM+User+WHERE+UserRoleId+!=+null+AND+ProfileId+!=+null+AND+UserRoleId+!=+null";
    
            // Token de portador
            String bearerToken = "00DUB000001QzdZ!AQEAQPOPzHkrB8kg4rHy0nCbg4vIu.2c1SyaeU9w.SujprDPE6T_PqfIPIKf0VN3zZZmeJqorGRRNUfOkyzrECd8ZLJVvDj_";
    
            // Realizar la consulta
            String response = executeQuery(queryUrl, bearerToken);
    
            // Procesar la respuesta y mostrar los IDs y los Names en la tabla
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