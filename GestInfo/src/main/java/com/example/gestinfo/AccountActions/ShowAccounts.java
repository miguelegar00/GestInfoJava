package com.example.gestinfo.AccountActions;

import java.io.IOException;

import com.example.gestinfo.DocumentGeneration.OpenUrlExampleFX;
import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.SalesforceTokenManager;
import com.example.gestinfo.GenericActions.ShowError;
import com.example.gestinfo.LoginAccountsActions.ChangeUsername;
import com.example.gestinfo.LoginAccountsActions.ResetPassword;
import com.example.gestinfo.UsersActions.ShowUsers;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class ShowAccounts extends Application {

    private static SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    public static TableView<Account> accountTable;
    private static Stage stage;

    public static Stage getStage() {
        return stage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {

        @SuppressWarnings("unused")
        String username = System.getProperty("Name");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
    
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
    
        Menu nuevoMenu = new Menu("Nuevo");
        MenuItem crearCuentaItem = new MenuItem("Crear nueva cuenta");
        crearCuentaItem.setOnAction(event -> CreateAccount.crearCuentaNueva());
        nuevoMenu.getItems().add(crearCuentaItem);
    
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
    
        menuBar.getMenus().addAll(salirMenu, nuevoMenu, usuariosMenu);
    
        root.setTop(menuBar);

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterAccounts(newValue));

        Button resetButton = new Button("Restablecer");
        resetButton.setOnAction(event -> {
            
            searchField.clear();
            SalesforceActions.executeAndDisplayResultsAccounts();
        });

        HBox searchContainer = new HBox(10, searchField, resetButton);
        searchContainer.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.setPadding(new Insets(0, 10, 10, 10));

        BorderPane.setMargin(searchContainer, new Insets(0, 0, 10, 0));

        VBox topContainer = new VBox(menuBar, searchContainer);
        topContainer.setSpacing(10);
        topContainer.setAlignment(Pos.CENTER);
        root.setTop(topContainer);

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
    
        Button generarDoc = new Button("Generar Documentos");
        generarDoc.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;");
        generarDoc.setOnAction(event -> generarDocumento());
    
        Button editarButton = new Button("Editar");
        editarButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        editarButton.setOnAction(event -> EditAccount.editar());
    
        HBox buttonBox = new HBox(generarDoc, editarButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER);
    
        root.setCenter(accountTable);
        root.setBottom(buttonBox);
    
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("GestInfo");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("https://parsers.vc/logo/c8924191-7868-46a7-ac6b-83be877cf3fe-3.png"));
        primaryStage.setMaximized(true);
        primaryStage.show();
    
        SalesforceActions.executeAndDisplayResultsAccounts();
    }


    private void filterAccounts(String searchQuery) {

        ObservableList<Account> accounts = accountTable.getItems();
        
        ObservableList<Account> filteredAccounts = FXCollections.observableArrayList();
        
        for (Account account : accounts) {
            if (account.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredAccounts.add(account);
            }
        }
        accountTable.setItems(filteredAccounts);
    }

    private void generarDocumento() {
        Account selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount == null) {
            ShowError.showError("Debes seleccionar una cuenta para generar los documentos.");
            return;
        }

        String accountId = selectedAccount.getId();

        OpenUrlExampleFX app = new OpenUrlExampleFX(accountId);

        Stage stage = new Stage();

        try {
            app.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cerrarSesion() {
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