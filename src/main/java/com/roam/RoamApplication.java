package com.roam;

import com.roam.service.DatabaseService;
import com.roam.service.SecurityContext;
import com.roam.service.SettingsService;
import com.roam.util.HibernateUtil;
import com.roam.util.ThemeManager;
import com.roam.view.LockScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RoamApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(RoamApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database FIRST
            logger.info("=".repeat(50));
            logger.info("🚀 Starting Roam Application");
            logger.info("=".repeat(50));

            DatabaseService.initializeDatabase();

            logger.info("=".repeat(50));

            // Load settings
            SettingsService settingsService = SettingsService.getInstance();
            String theme = settingsService.getSettings().getTheme();

            // Apply AtlantaFX theme using ThemeManager
            ThemeManager.getInstance().applyTheme(theme);

            // Load custom fonts
            Font regularFont = loadFonts();
            Font boldFont = Font.font("Poppins Bold", 14);

            // Create main layout with fonts
            MainLayout mainLayout = new MainLayout(regularFont, boldFont);

            // Load custom CSS (will complement AtlantaFX)
            String css = Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm();

            // Check security - only show lock screen if both enabled AND PIN is set
            String pinHash = settingsService.getSettings().getPinHash();
            boolean hasPinSet = pinHash != null && !pinHash.isEmpty();

            if (SecurityContext.getInstance().isLockEnabled() && hasPinSet) {
                LockScreen lockScreen = new LockScreen(() -> {
                    Scene scene = new Scene(mainLayout, 1024, 600);
                    scene.getStylesheets().add(css);
                    primaryStage.setScene(scene);
                    primaryStage.centerOnScreen();
                });

                Scene lockScene = new Scene(lockScreen, 1024, 600);
                lockScene.getStylesheets().add(css);
                primaryStage.setScene(lockScene);
            } else {
                // No lock screen - proceed directly to main app
                Scene scene = new Scene(mainLayout, 1024, 600);
                scene.getStylesheets().add(css);
                primaryStage.setScene(scene);
                SecurityContext.getInstance().setAuthenticated(true); // Auto-authenticate when no lock
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
                logger.warn("Failed to load application icon: {}", e.getMessage());
            }

            // Show window
            primaryStage.show();

            logger.info("✓ Application started successfully");

        } catch (Exception e) {
            logger.error("✗ Failed to start application: {}", e.getMessage(), e);
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
        logger.info("=".repeat(50));
        logger.info("🛑 Shutting down Roam Application");
        logger.info("=".repeat(50));
        HibernateUtil.shutdown();
        logger.info("✓ Application shutdown complete");
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
            logger.info("✓ Fonts loaded successfully");
        } catch (Exception e) {
            logger.error("✗ Failed to load fonts: {}", e.getMessage());
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