package com.example.gestinfo.DocumentGeneration;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Miguel
 */
public class OpenUrlExampleFX extends Application {

    private Map<String, String> urlMap;
    private String accountId;

    public OpenUrlExampleFX(String accountId) {
        this.accountId = accountId;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Generador de Documentos");

        // Define los nombres de los documentos y los enlaces de cada uno 
        urlMap = new HashMap<>();
        urlMap.put("Solicitud Concurso Con Masa", "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/SolicitudConcursoConMasa?id=" + this.accountId);
        urlMap.put("Solicitud Concurso Sin Masa", "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/SolicitudConcursoSinMasa?id=" + this.accountId);
        urlMap.put("Inventario de bienes y servicios", "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/LSOBienesYServicios?id=" + this.accountId);
        urlMap.put("EPI", "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/EPI?id=" + this.accountId);
        urlMap.put("Baja Activa LSF", "https://solucionamideuda--devmiguel--c.sandbox.vf.force.com/apex/BajaActivaLSOVF?id=" + this.accountId);

        // Configura la picklist
        ComboBox<String> pickList = new ComboBox<>();
        pickList.setItems(FXCollections.observableArrayList(urlMap.keySet()));
        pickList.setPromptText("Seleccione una opción");

        // Configura el botón de generación de los documentos
        Button generateButton = new Button("Generar documento");
        generateButton.setOnAction(event -> {
            String selectedExample = pickList.getValue();
            if (selectedExample != null) {
                String url = urlMap.get(selectedExample);
                if (url != null) {
                    showSuccess ("Documento generado correctamente");
                    OpenUrlExample.openUrlInBrowser(url);
                }
            }
        });

        VBox vbox = new VBox(10, pickList, generateButton);
        vbox.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(vbox, 300, 150));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}