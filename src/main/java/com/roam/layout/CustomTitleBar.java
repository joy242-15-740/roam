package com.roam.layout;

import com.roam.service.SecurityContext;
import com.roam.util.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Custom title bar with Roam icon, window controls, and dark/light mode
 * support.
 * Provides drag-to-move and double-click-to-maximize functionality.
 */
public class CustomTitleBar extends HBox {

    private static final Logger logger = LoggerFactory.getLogger(CustomTitleBar.class);

    private static final double TITLE_BAR_HEIGHT = 38;
    private static final double ICON_SIZE = 20;
    private static final double BUTTON_SIZE = 46;
    private static final double ACTION_BUTTON_SIZE = 32;

    private final Stage stage;
    private final ImageView iconView;
    private final Button minimizeBtn;
    private final Button maximizeBtn;
    private final Button closeBtn;

    // Action buttons (moved from sidebar)
    private final Button settingsBtn;
    private final Button lockBtn;
    private final Button refreshBtn;

    // Callback for navigation
    private Consumer<String> onNavigate;

    // Callback for showing lock screen
    private Runnable onShowLockScreen;

    // For window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    // For maximize/restore
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreWidth, restoreHeight;

    public CustomTitleBar(Stage stage) {
        this.stage = stage;

        getStyleClass().add("custom-title-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(TITLE_BAR_HEIGHT);
        setMinHeight(TITLE_BAR_HEIGHT);
        setMaxHeight(TITLE_BAR_HEIGHT);
        setPadding(new Insets(0, 0, 0, 12));
        setSpacing(10);

        // App icon
        iconView = createAppIcon();

        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create action buttons (Settings, Lock, Refresh)
        settingsBtn = createActionButton(Material2MZ.SETTINGS, "settings-btn", this::openSettings);
        lockBtn = createActionButton(Feather.LOCK, "lock-btn", this::showLockScreen);
        refreshBtn = createActionButton(Feather.ROTATE_CW, "refresh-btn", this::resetToLockScreen);

        // Action buttons container
        HBox actionBox = new HBox(4);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(0, 16, 0, 0));
        actionBox.getChildren().addAll(settingsBtn, lockBtn, refreshBtn);

        // Separator
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setMinWidth(1);
        separator.setMaxWidth(1);
        separator.setPrefHeight(20);
        separator.getStyleClass().add("title-bar-separator");

        // Window control buttons
        minimizeBtn = createControlButton(Feather.MINUS, "minimize-btn", this::minimizeWindow);
        maximizeBtn = createControlButton(Feather.SQUARE, "maximize-btn", this::toggleMaximize);
        closeBtn = createControlButton(Feather.X, "close-btn", this::closeWindow);
        closeBtn.getStyleClass().add("close-button");

        // Button container
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(minimizeBtn, maximizeBtn, closeBtn);

        getChildren().addAll(iconView, spacer, actionBox, separator, buttonBox);

        // Enable window dragging
        setupWindowDragging();

        // Listen for theme changes
        applyTheme();

        logger.debug("CustomTitleBar initialized");
    }

    private ImageView createAppIcon() {
        ImageView imageView = new ImageView();
        try {
            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/roam-icon.png")));
            imageView.setImage(icon);
            imageView.setFitWidth(ICON_SIZE);
            imageView.setFitHeight(ICON_SIZE);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        } catch (Exception e) {
            logger.warn("Failed to load app icon: {}", e.getMessage());
        }
        return imageView;
    }

    private Button createControlButton(Feather icon, String styleClass, Runnable action) {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(12);
        fontIcon.getStyleClass().add("title-bar-icon");
        button.setGraphic(fontIcon);
        button.getStyleClass().addAll("title-bar-button", styleClass);
        button.setPrefSize(BUTTON_SIZE, TITLE_BAR_HEIGHT);
        button.setMinSize(BUTTON_SIZE, TITLE_BAR_HEIGHT);
        button.setMaxSize(BUTTON_SIZE, TITLE_BAR_HEIGHT);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Button createActionButton(Feather icon, String styleClass, Runnable action) {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(14);
        fontIcon.getStyleClass().add("title-bar-action-icon");
        button.setGraphic(fontIcon);
        button.getStyleClass().addAll("title-bar-action-button", styleClass);
        button.setPrefSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setMinSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setMaxSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Button createActionButton(Material2MZ icon, String styleClass, Runnable action) {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.getStyleClass().add("title-bar-action-icon");
        button.setGraphic(fontIcon);
        button.getStyleClass().addAll("title-bar-action-button", styleClass);
        button.setPrefSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setMinSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setMaxSize(ACTION_BUTTON_SIZE, ACTION_BUTTON_SIZE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private void openSettings() {
        if (onNavigate != null) {
            onNavigate.accept("settings");
        }
    }

    /**
     * Show the lock screen immediately.
     * If lock is enabled, shows the lock screen.
     * If lock is not enabled, shows a message.
     */
    private void showLockScreen() {
        if (SecurityContext.getInstance().isLockEnabled()) {
            // Set not authenticated to require PIN entry
            SecurityContext.getInstance().setAuthenticated(false);

            // If callback is set, use it to show lock screen
            if (onShowLockScreen != null) {
                onShowLockScreen.run();
            } else {
                // Fallback: relaunch app to show lock screen
                relaunchApp();
            }
        } else {
            // Lock is not enabled - show info message
            Alert alert = ThemeManager.getInstance().createAlert(
                    Alert.AlertType.INFORMATION,
                    "Lock Screen",
                    "Lock screen is not enabled",
                    "Enable the lock screen in Settings to use this feature.");
            alert.showAndWait();
        }
    }

    /**
     * Reset to lock screen - shows the lock screen regardless of current state.
     * This is the refresh/reset button behavior.
     */
    private void resetToLockScreen() {
        if (SecurityContext.getInstance().isLockEnabled()) {
            // Set not authenticated to require PIN entry
            SecurityContext.getInstance().setAuthenticated(false);

            // If callback is set, use it to show lock screen
            if (onShowLockScreen != null) {
                onShowLockScreen.run();
            } else {
                // Fallback: relaunch app
                relaunchApp();
            }
        } else {
            // Lock is not enabled - offer to enable it or just show info
            Alert alert = ThemeManager.getInstance().createAlert(
                    Alert.AlertType.INFORMATION,
                    "Reset",
                    "Lock screen is not enabled",
                    "Enable the lock screen in Settings first, then use this button to quickly lock your session.");
            alert.showAndWait();
        }
    }

    /**
     * Relaunch the application.
     * Restarts the JavaFX application to show lock screen.
     */
    private void relaunchApp() {
        try {
            // Get the current process info
            String javaBin = System.getProperty("java.home") +
                    java.io.File.separator + "bin" +
                    java.io.File.separator + "java";

            // Get the classpath
            String classpath = System.getProperty("java.class.path");

            // Get the main class
            String mainClass = "com.roam.RoamApplication";

            // Build the command
            ProcessBuilder builder = new ProcessBuilder(
                    javaBin, "-cp", classpath, mainClass);

            builder.inheritIO();
            builder.start();

            // Exit current instance
            Platform.exit();

        } catch (Exception e) {
            logger.error("Failed to relaunch application: {}", e.getMessage());

            // Show error and suggest manual restart
            Alert alert = ThemeManager.getInstance().createAlert(
                    Alert.AlertType.ERROR,
                    "Relaunch Failed",
                    "Could not relaunch application",
                    "Please close and reopen the application manually.");
            alert.showAndWait();
        }
    }

    private void setupWindowDragging() {
        // Drag to move window
        setOnMousePressed(event -> {
            if (!isMaximized) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        setOnMouseDragged(event -> {
            if (isMaximized) {
                // Restore window when dragging from maximized state
                double mouseX = event.getScreenX();
                double mouseY = event.getScreenY();

                // Calculate relative position in the title bar
                double relativeX = xOffset / stage.getWidth();

                // Restore to previous size
                isMaximized = false;
                stage.setWidth(restoreWidth);
                stage.setHeight(restoreHeight);

                // Position window so mouse is at same relative position
                stage.setX(mouseX - (restoreWidth * relativeX));
                stage.setY(mouseY - yOffset);

                updateMaximizeIcon();

                // Update offsets for continued dragging
                xOffset = restoreWidth * relativeX;
            } else {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Double-click to maximize/restore
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize();
            }
        });
    }

    private void minimizeWindow() {
        stage.setIconified(true);
    }

    private void toggleMaximize() {
        if (isMaximized) {
            // Restore to previous size and position
            stage.setX(restoreX);
            stage.setY(restoreY);
            stage.setWidth(restoreWidth);
            stage.setHeight(restoreHeight);
            isMaximized = false;
        } else {
            // Save current bounds for restore
            restoreX = stage.getX();
            restoreY = stage.getY();
            restoreWidth = stage.getWidth();
            restoreHeight = stage.getHeight();

            // Get the screen bounds (visual bounds exclude taskbar)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Maximize to fill screen
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            isMaximized = true;
        }
        updateMaximizeIcon();
    }

    private void updateMaximizeIcon() {
        FontIcon icon = (FontIcon) maximizeBtn.getGraphic();
        if (isMaximized) {
            icon.setIconCode(Feather.COPY); // Use copy icon to represent restore
        } else {
            icon.setIconCode(Feather.SQUARE);
        }
    }

    private void closeWindow() {
        stage.close();
    }

    /**
     * Check if window is currently maximized
     */
    public boolean isMaximized() {
        return isMaximized;
    }

    /**
     * Set the navigation callback for settings button
     */
    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    /**
     * Set the callback for showing lock screen
     */
    public void setOnShowLockScreen(Runnable onShowLockScreen) {
        this.onShowLockScreen = onShowLockScreen;
    }

    /**
     * Apply current theme styling
     */
    public void applyTheme() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();

        if (isDark) {
            setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10 10 0 0;");
        } else {
            setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10 10 0 0;");
        }
    }

    /**
     * Update theme when it changes
     */
    public void refreshTheme() {
        applyTheme();
    }

}
