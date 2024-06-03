package com.example.gestinfo.AccountActions;

import java.io.IOException;

import com.example.gestinfo.GenericActions.SalesforceActions;
import com.example.gestinfo.GenericActions.ShowError;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class CreateAccount {

    static void crearCuentaNueva() {
        
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<String> clienteDeComboBox = new ComboBox<>();
        ComboBox<String> genderComboBox = new ComboBox<>();
        ComboBox<String> recordTypeComboBox = new ComboBox<>();
        
        clienteDeComboBox.getItems().addAll("LSO", "Programa");
        
        genderComboBox.getItems().addAll("Don", "Do\u00F1a");
        
        recordTypeComboBox.getItems().addAll("Cliente potencial", "Cliente");
        
        Button crearButton = new Button("Crear");
        crearButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        crearButton.setOnAction(event -> {
            
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String clienteDe = clienteDeComboBox.getValue();
            String gender = genderComboBox.getValue();
            String recordType = recordTypeComboBox.getValue();
        
            if (recordType == null || recordType.isEmpty()) {
                ShowError.showError("Por favor, seleccione un Record Type.");
                return;
            }
        
            try {
                String createUrl = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Account/";
        
                String data = "{\"FirstName\": \"" + firstName + "\", " +
                        "\"LastName\": \"" + lastName + "\", " +
                        "\"PersonEmail\": \"" + email + "\", " +
                        "\"RecordTypeId\": \"" + getRecordTypeId(recordType) + "\", " +
                        "\"Phone\": \"" + phone + "\", " +
                        "\"Cliente_de__c\": \"" + clienteDe + "\", " +
                        "\"Gender__pc\": \"" + gender.replace("ñ", "\\u00F1") + "\"}";
        
        
                SalesforceActions.executePostRequest(createUrl, data);
        
                SalesforceActions.executeAndDisplayResultsAccounts();
        
                Stage stage = (Stage) crearButton.getScene().getWindow();
                stage.close();
        
            } catch (IOException e) {
                e.printStackTrace();
                ShowError.showError("Error al crear la cuenta: " + e.getMessage());
            }
        });
        
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
                new Label("Género:"),
                genderComboBox,
                new Label("Record Type:"),
                recordTypeComboBox
        );
        
        HBox buttonContainer = new HBox(crearButton);
        buttonContainer.setAlignment(Pos.CENTER);
        
        createRoot.getChildren().add(buttonContainer);
        
        Scene createScene = new Scene(createRoot, 400, 640);
        Stage createStage = new Stage();
        createStage.setScene(createScene);
        createStage.setTitle("Crear nueva cuenta");
        createStage.show();
    }

    private static String getRecordTypeId(String recordTypeName) {
        if (recordTypeName.equals("Cliente potencial")) {
            return "0127Q000000yrwrQAA";
        } else if (recordTypeName.equals("Cliente")) {
            return "0127Q000000yrwqQAA";
        }
        return null;
    }
}
