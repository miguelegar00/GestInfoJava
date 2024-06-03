package com.example.gestinfo.UsersActions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.example.gestinfo.ShowCases;
import com.example.gestinfo.AccountActions.ShowAccounts;
import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.SalesforceTokenManager;
import com.example.gestinfo.LoginAccountsActions.ChangeUsername;
import com.example.gestinfo.LoginAccountsActions.ResetPassword;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class ShowUsers extends Application {

    ShowCases showCases = new ShowCases();
    
    private static SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    public static TableView<User> userTable;
    private static TextField searchField;

    public static ObservableList<User> originalUserList;

    static final Map<String, String> roleNames = new HashMap<>();

    static {
        try {
            // Cargar los roles desde el txt
            loadRoleNames("/com/example/gestinfo/Resources/roles.txt");
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
                    roleNames.put(parts[0], SalesforceActions.decodeString(parts[1]));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {
        
        VBox root = new VBox();
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #f0f0f0;");

        MenuBar menuBar = new MenuBar();

        Menu salirMenu = new Menu("Mi cuenta");
        MenuItem cerrarSesionItem = new MenuItem("Salir del programa");
        cerrarSesionItem.setOnAction(event -> cerrarSesion());
        MenuItem cambiarContrasenaItem = new MenuItem("Cambiar contraseña");
        cambiarContrasenaItem.setOnAction(event -> {
            try {
                ResetPassword.mostrarFormularioCambiarContrasena(tokenManager);
            } catch (IOException e) {
            }
        });
        MenuItem cambiarUsername = new MenuItem("Cambiar nombre de usuario");
        cambiarUsername.setOnAction(event -> {
            try {
                ChangeUsername.mostrarFormularioCambiarUsername(tokenManager);
            } catch (IOException e) {
            }
        });
        salirMenu.getItems().addAll(cambiarUsername, cambiarContrasenaItem, cerrarSesionItem);

        
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
            
            searchField.clear();
            userTable.setItems(originalUserList);
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

        SalesforceActions.executeAndDisplayResultsUsers(primaryStage);

        VBox.setVgrow(userTable, Priority.ALWAYS);

        Button editButton = new Button("Editar");
        editButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        editButton.setOnAction(event -> EditUsers.editUser());

        
        Button activateButton = new Button("Activar");
        activateButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        activateButton.setOnAction(event -> ActivateDesactivateUsers.activateUser());

        Button desactivateButton = new Button("Desactivar");
        desactivateButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        desactivateButton.setOnAction(event -> ActivateDesactivateUsers.desactivateUser());

        Button caseButton = new Button("Ver casos");
        caseButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
        caseButton.setOnAction(event -> ShowUsersCases.showCases());
        
        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(10);
        buttonContainer.getChildren().addAll(activateButton, editButton, desactivateButton, caseButton);

        VBox.setMargin(buttonContainer, new Insets(0, 0, 10, 0));

        root.getChildren().addAll(menuBar, searchContainer, userTable, buttonContainer);

        Scene scene = new Scene(root);

        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));

        primaryStage.setScene(scene);
        primaryStage.setTitle("GestInfo");
        primaryStage.show();

        Stage salesforceOAuthStage = ShowAccounts.getStage();
        if (salesforceOAuthStage != null) {
            salesforceOAuthStage.close();
        }
    }
    
    public static <K, V> K getKeyFromValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void cerrarSesion() {
        System.exit(0);
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