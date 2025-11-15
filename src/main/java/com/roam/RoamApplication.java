package com.roam;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class RoamApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load custom fonts
        Font regularFont = loadFonts();
        Font boldFont = Font.font("Poppins Bold", 14);

        // Create main layout with fonts
        MainLayout mainLayout = new MainLayout(regularFont, boldFont);

        // Create scene
        Scene scene = new Scene(mainLayout, 200, 200);

        // Load CSS
        String css = Objects.requireNonNull(
                getClass().getResource("/styles/application.css")).toExternalForm();
        scene.getStylesheets().add(css);

        // Set window properties
        primaryStage.setTitle(" "); // Set empty title for minimalistic look
        primaryStage.setScene(scene);

        // Set minimum window size
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);

        // Set application icon
        try {
            Image icon = new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/icons/roam-icon.png")));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        // Show window
        primaryStage.show();
    }

    private Font loadFonts() {
        Font regularFont = null;
        try {
            regularFont = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Regular.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Medium.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-SemiBold.ttf"), 14);
            Font.loadFont(
                    getClass().getResourceAsStream("/fonts/Poppins-Bold.ttf"), 14);
            System.out.println("✓ Fonts loaded successfully");
        } catch (Exception e) {
            System.err.println("✗ Failed to load fonts: " + e.getMessage());
        }
        return regularFont;
    }

    public static void main(String[] args) {
        launch(args);
    }
}