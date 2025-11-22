package com.roam;

import com.roam.service.DatabaseService;
import com.roam.service.SecurityContext;
import com.roam.service.SettingsService;
import com.roam.util.HibernateUtil;
import com.roam.view.LockScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class RoamApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database FIRST
            System.out.println("=".repeat(50));
            System.out.println("🚀 Starting Roam Application");
            System.out.println("=".repeat(50));

            DatabaseService.initializeDatabase();

            System.out.println("=".repeat(50));

            // Load custom fonts
            Font regularFont = loadFonts();
            Font boldFont = Font.font("Poppins Bold", 14);

            // Create main layout with fonts
            MainLayout mainLayout = new MainLayout(regularFont, boldFont);

            // Load CSS
            String css = Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm();

            // Load settings
            SettingsService settingsService = SettingsService.getInstance();
            String theme = settingsService.getSettings().getTheme();
            boolean isDark = "Dark".equalsIgnoreCase(theme);

            // Check security
            if (SecurityContext.getInstance().isLockEnabled()) {
                LockScreen lockScreen = new LockScreen(() -> {
                    Scene scene = new Scene(mainLayout, 1024, 600);
                    scene.getStylesheets().add(css);
                    if (isDark) {
                        scene.getRoot().getStyleClass().add("dark");
                    }
                    primaryStage.setScene(scene);
                    primaryStage.centerOnScreen();
                });

                Scene lockScene = new Scene(lockScreen, 1024, 600);
                lockScene.getStylesheets().add(css);
                if (isDark) {
                    lockScene.getRoot().getStyleClass().add("dark");
                }
                primaryStage.setScene(lockScene);
            } else {
                // Create scene
                Scene scene = new Scene(mainLayout, 1024, 600);
                scene.getStylesheets().add(css);
                if (isDark) {
                    scene.getRoot().getStyleClass().add("dark");
                }
                primaryStage.setScene(scene);
            }

            // Set window properties
            primaryStage.setTitle("Roam");

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

            System.out.println("✓ Application started successfully");

        } catch (Exception e) {
            System.err.println("✗ Failed to start application: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Database Error",
                    "Failed to initialize database. The application will now close.",
                    e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Shutdown Hibernate on application exit
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🛑 Shutting down Roam Application");
        System.out.println("=".repeat(50));
        HibernateUtil.shutdown();
        System.out.println("✓ Application shutdown complete");
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

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}