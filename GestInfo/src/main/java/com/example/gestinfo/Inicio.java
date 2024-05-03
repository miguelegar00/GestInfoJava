package com.example.gestinfo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Inicio extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crear la imagen y el ImageView
        Image image = new Image("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Salesforce.com_logo.svg/2560px-Salesforce.com_logo.svg.png");
        ImageView imageView = new ImageView(image);

        // Configurar el tamaño del ImageView
        imageView.setFitWidth(300); // Ancho de la imagen
        imageView.setFitHeight(200); // Alto de la imagen
        imageView.setPreserveRatio(true); // Mantener la proporción de la imagen al cambiar el tamaño

        // Centrar la imagen verticalmente y horizontalmente
        StackPane imagePane = new StackPane(imageView);
        imagePane.setAlignment(Pos.CENTER);

        // Crear el botón "Iniciar" con estilo CSS personalizado
        Button accederButton = new Button("Acceder");
        accederButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-padding: 10 20; -fx-border-color: transparent; -fx-border-radius: 5;");
        accederButton.setOnAction(event -> {
            // Ejecutar la clase SalesforceOAuth
            SalesforceOAuth salesforceOAuth = new SalesforceOAuth();
            try {
                salesforceOAuth.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Cerrar la ventana actual
            primaryStage.close();
        });

        // Colocar el botón en la esquina inferior derecha y centrarlo horizontalmente
        HBox buttonContainer = new HBox(accederButton);
        buttonContainer.setAlignment(Pos.BOTTOM_CENTER);
        buttonContainer.setPadding(new Insets(10));

        // Configurar el contenedor principal
        BorderPane root = new BorderPane();
        root.setCenter(imagePane); // Colocar la imagen en el centro
        root.setBottom(buttonContainer); // Colocar el botón en la parte inferior derecha

        // Configurar la escena y mostrar la ventana
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Soluciona Mi Deuda");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
