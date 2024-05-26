package com.example.gestinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.example.gestinfo.ShowUsers.User;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class ShowCases {

    private SalesforceTokenManager tokenManager = new SalesforceTokenManager();

    @SuppressWarnings("unchecked")
    public void abrirVentanaCasos(User selectedUser) {
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
            String bearerToken = tokenManager.getNewAccessToken();
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

        Scene caseScene = new Scene(caseRoot, 450, 300);
        caseStage.setScene(caseScene);
        caseStage.showAndWait();
    }

    private void actualizarEstadoCaso(CaseInfo selectedCase, String newStatus) throws IOException {
        String url = "https://solucionamideuda--devmiguel.sandbox.my.salesforce.com/services/data/v60.0/sobjects/Case/" + selectedCase.getCaseId();
        String bearerToken = tokenManager.getNewAccessToken();
        String data = "{\"Status\": \"" + newStatus + "\"}";
    
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

    private static String decodeString(String encodedString) {
        try {
            return java.net.URLDecoder.decode(encodedString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return encodedString;
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
                
                Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationDialog.setTitle("Confirmación de cambio de estado");
                confirmationDialog.setHeaderText("¿Estás seguro de que quieres cambiar el estado del caso?");
    
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
    
        @SuppressWarnings("exports")
        public SimpleStringProperty subjectProperty() {
            return subject;
        }
    
        public String getStage() {
            return stage.get();
        }
    
        public void setStage(String stage) {
            this.stage.set(stage);
        }
    
        @SuppressWarnings("exports")
        public SimpleStringProperty stageProperty() {
            return stage;
        }
    }
}