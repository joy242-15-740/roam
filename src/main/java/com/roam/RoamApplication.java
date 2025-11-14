package com.roam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class RoamApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load custom font
        loadFonts();

        // Create root container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #FFFFFF;");

        // Create scene with comfortable default size
        Scene scene = new Scene(root, 100, 100);

        // Load CSS
        String css = Objects.requireNonNull(
                getClass().getResource("/styles/application.css")
        ).toExternalForm();
        scene.getStylesheets().add(css);

        // Set window properties
        primaryStage.setTitle("Roam - Personal Knowledge Management");
        primaryStage.setScene(scene);

        // Set minimum window size
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);

        // Set application icon
        try {
            Image icon = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/icons/roam-icon.png")
                    )
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        // Show window
        primaryStage.show();
    }

    private void loadFonts() {
        try {
            javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 14
            );
            javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Medium.ttf"), 14
            );
            javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-SemiBold.ttf"), 14
            );
            javafx.scene.text.Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"), 14
            );
            System.out.println("Fonts loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load fonts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}