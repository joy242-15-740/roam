package com.roam.util;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ThemeManager {

    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    private static ThemeManager instance;
    private String currentTheme = "Light";
    private Scene mainScene;

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Set the main scene reference for theme management
     */
    public void setMainScene(Scene scene) {
        this.mainScene = scene;
        // Apply current theme to the scene
        applyDarkClass(scene.getRoot(), isDarkTheme());
    }

    public void applyTheme(String themeName) {
        this.currentTheme = themeName;

        switch (themeName.toLowerCase()) {
            case "dark":
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                break;
            case "nord light":
                Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                break;
            case "nord dark":
                Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
                break;
            case "cupertino light":
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                break;
            case "cupertino dark":
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                break;
            case "dracula":
                Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
                break;
            case "light":
            default:
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                break;
        }

        // Apply dark class to main scene if it exists
        if (mainScene != null) {
            applyDarkClass(mainScene.getRoot(), isDarkTheme());
        }

        logger.info("✓ Applied AtlantaFX theme: {}", themeName);
    }

    /**
     * Apply or remove dark class from a parent node
     */
    public void applyDarkClass(Parent root, boolean isDark) {
        if (root == null)
            return;

        if (isDark) {
            if (!root.getStyleClass().contains("dark")) {
                root.getStyleClass().add("dark");
            }
        } else {
            root.getStyleClass().remove("dark");
        }
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkTheme() {
        return currentTheme.toLowerCase().contains("dark") ||
                currentTheme.equalsIgnoreCase("dracula");
    }

    /**
     * Style a dialog for the current theme.
     * Call this after creating any Dialog or Alert to ensure proper theming.
     */
    public void styleDialog(Dialog<?> dialog) {
        if (dialog == null)
            return;

        DialogPane dialogPane = dialog.getDialogPane();

        // Add stylesheet
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource("/styles/application.css")).toExternalForm();
            if (!dialogPane.getStylesheets().contains(css)) {
                dialogPane.getStylesheets().add(css);
            }
        } catch (Exception e) {
            logger.warn("Could not add stylesheet to dialog: {}", e.getMessage());
        }

        // Apply dark class if needed
        boolean isDark = isDarkTheme();
        applyDarkClass(dialogPane, isDark);

        // Set explicit inline styles for dark mode to ensure visibility
        // (CSS variables don't work well in separate dialog windows)
        if (isDark) {
            dialogPane.setStyle(
                    "-fx-background-color: #1e1e1e;");

            // Style header text
            if (dialogPane.getHeader() != null) {
                dialogPane.getHeader().setStyle("-fx-text-fill: #ffffff;");
            }
        } else {
            dialogPane.setStyle("");
        }
    }

    /**
     * Style an Alert for the current theme.
     */
    public void styleAlert(Alert alert) {
        styleDialog(alert);
    }

    /**
     * Create and style an Alert with proper theming.
     */
    public Alert createAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }

    /**
     * Create and style an Alert with just content.
     */
    public Alert createAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setContentText(content);
        styleAlert(alert);
        return alert;
    }
}
