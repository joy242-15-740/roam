package com.roam.view;

import com.roam.service.SecurityContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Lock screen for PIN authentication.
 * Updated to support 8+ character PINs with BCrypt verification.
 */
public class LockScreen extends StackPane {

    private final Runnable onUnlock;
    private final PasswordField pinField;
    private final Label messageLabel;
    private final Button unlockButton;

    public LockScreen(Runnable onUnlock) {
        this.onUnlock = onUnlock;
        this.setStyle("-fx-background-color: -fx-bg-base;");

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);
        content.setPadding(new Insets(50));

        // Icon
        Label icon = new Label("🔒");
        icon.setStyle("-fx-font-size: 64px;");

        // Title
        Label title = new Label("Roam is Locked");
        title.setFont(Font.font("Poppins Bold", 28));
        title.setStyle("-fx-text-fill: -roam-text-primary;");

        // Subtitle
        Label subtitle = new Label("Enter your PIN to unlock");
        subtitle.setFont(Font.font("Poppins Regular", 14));
        subtitle.setStyle("-fx-text-fill: -roam-text-secondary;");

        // PIN Field
        pinField = new PasswordField();
        pinField.setPromptText("Enter PIN (" + SecurityContext.getMinPinLength() + "+ characters)");
        pinField.setFont(Font.font("Poppins Regular", 16));
        pinField.setPrefHeight(50);
        pinField.setMaxWidth(300);
        pinField.setStyle(
                "-fx-border-color: -roam-border; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-background-color: -roam-bg-primary;");

        // Handle Enter key
        pinField.setOnAction(e -> checkPin());

        // Message Label
        messageLabel = new Label("");
        messageLabel.setFont(Font.font("Poppins Regular", 13));
        messageLabel.setStyle("-fx-text-fill: #C62828;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setAlignment(Pos.CENTER);

        // Unlock Button
        unlockButton = new Button("Unlock");
        unlockButton.setFont(Font.font("Poppins Medium", 16));
        unlockButton.setPrefSize(150, 45);
        unlockButton.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        unlockButton.setOnAction(e -> checkPin());

        content.getChildren().addAll(icon, title, subtitle, pinField, messageLabel, unlockButton);
        getChildren().add(content);

        // Focus on PIN field
        pinField.requestFocus();
    }

    private void checkPin() {
        String pin = pinField.getText();

        if (pin.isEmpty()) {
            showError("Please enter your PIN");
            return;
        }

        if (!SecurityContext.isValidPinFormat(pin)) {
            showError("PIN must be at least " + SecurityContext.getMinPinLength() + " characters");
            return;
        }

        try {
            if (SecurityContext.getInstance().authenticate(pin)) {
                // Success
                messageLabel.setStyle("-fx-text-fill: #2E7D32;");
                messageLabel.setText("✓ Unlocked successfully");

                // Small delay before unlocking for UX
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(500), event -> onUnlock.run()));
                timeline.play();

            } else {
                // Failed authentication
                int attempts = SecurityContext.getInstance().getFailedAttempts();
                showError("Incorrect PIN (attempt " + attempts + ")");
                shakeAnimation();
                pinField.clear();
                pinField.requestFocus();
            }
        } catch (SecurityException e) {
            // Rate limit exceeded
            showError(e.getMessage());
            pinField.setDisable(true);
            unlockButton.setDisable(true);

            // Re-enable after 60 seconds
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(60), event -> {
                        pinField.setDisable(false);
                        unlockButton.setDisable(false);
                        messageLabel.setText("");
                        pinField.requestFocus();
                    }));
            timeline.play();
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #C62828;");
        messageLabel.setText(message);
    }

    private void shakeAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), new javafx.animation.KeyValue(translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new javafx.animation.KeyValue(translateXProperty(), -10)),
                new KeyFrame(Duration.millis(100), new javafx.animation.KeyValue(translateXProperty(), 10)),
                new KeyFrame(Duration.millis(150), new javafx.animation.KeyValue(translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new javafx.animation.KeyValue(translateXProperty(), 10)),
                new KeyFrame(Duration.millis(250), new javafx.animation.KeyValue(translateXProperty(), 0)));
        timeline.play();
    }
}
