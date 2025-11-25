package com.roam.view;

import atlantafx.base.theme.Styles;
import com.roam.service.SecurityContext;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Lock screen for PIN authentication.
 * Updated to match the clean UI design with numeric keypad.
 */
public class LockScreen extends StackPane {

    private final Runnable onUnlock;
    private final Label messageLabel;
    private final List<Circle> pinDots;
    private final StringBuilder currentPin;
    private final int PIN_LENGTH = 8; // Fixed length for UI, though backend supports more

    public LockScreen(Runnable onUnlock) {
        this.onUnlock = onUnlock;
        this.currentPin = new StringBuilder();
        this.pinDots = new ArrayList<>();

        this.setStyle("-fx-background-color: -roam-bg-primary;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(320);
        content.setPadding(new Insets(30));

        // Icon
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setStyle(
                "-fx-background-color: -roam-blue-light; -fx-background-radius: 50%; -fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60;");

        FontIcon lockIcon = new FontIcon(Feather.LOCK);
        lockIcon.setIconSize(24);
        lockIcon.setIconColor(javafx.scene.paint.Color.web("#2563EB")); // Roam Blue
        iconBox.getChildren().add(lockIcon);

        // Title
        Label title = new Label("App Locked");
        title.setFont(Font.font("Poppins Bold", 20));
        title.setStyle("-fx-text-fill: -roam-text-primary;");

        // Subtitle
        Label subtitle = new Label("Enter your PIN to access Roam");
        subtitle.setFont(Font.font("Poppins Regular", 12));
        subtitle.setStyle("-fx-text-fill: -roam-text-secondary;");

        // PIN Dots
        HBox dotsContainer = new HBox(8);
        dotsContainer.setAlignment(Pos.CENTER);
        dotsContainer.setPadding(new Insets(15, 0, 15, 0));

        for (int i = 0; i < PIN_LENGTH; i++) {
            Circle dot = new Circle(4);
            dot.getStyleClass().add("pin-dot");
            pinDots.add(dot);
            dotsContainer.getChildren().add(dot);
        }

        // Keypad
        GridPane keypad = createKeypad();

        // Message Label
        messageLabel = new Label("");
        messageLabel.setFont(Font.font("Poppins Regular", 13));
        messageLabel.setStyle("-fx-text-fill: -roam-red;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setAlignment(Pos.CENTER);

        // Footer
        Label footer = new Label("Roam Security");
        footer.setFont(Font.font("Poppins Regular", 12));
        footer.setStyle("-fx-text-fill: -roam-text-hint;");
        VBox.setMargin(footer, new Insets(30, 0, 0, 0));

        content.getChildren().addAll(iconBox, title, subtitle, dotsContainer, messageLabel, keypad, footer);
        getChildren().add(content);

        // Add listeners for responsive scaling
        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleContent(content));
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleContent(content));
    }

    private void scaleContent(VBox content) {
        double width = getWidth();
        double height = getHeight();

        // Use layout bounds to get the actual size of the content
        double contentWidth = content.getLayoutBounds().getWidth();
        double contentHeight = content.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        // Calculate scale factors
        double scaleX = width < contentWidth + 40 ? (width - 40) / contentWidth : 1.0;
        double scaleY = height < contentHeight + 40 ? (height - 40) / contentHeight : 1.0;

        // Use the smaller scale to maintain aspect ratio and fit within bounds
        double scale = Math.min(scaleX, scaleY);

        content.setScaleX(scale);
        content.setScaleY(scale);
    }

    private GridPane createKeypad() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int number = 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = createKeypadButton(String.valueOf(number));
                grid.add(btn, col, row);
                number++;
            }
        }

        // Bottom row
        Button zeroBtn = createKeypadButton("0");
        grid.add(zeroBtn, 1, 3);

        Button backspaceBtn = new Button();
        FontIcon backIcon = new FontIcon(Feather.DELETE);
        backIcon.setIconSize(20);
        backspaceBtn.setGraphic(backIcon);
        backspaceBtn.getStyleClass().add("keypad-button");
        backspaceBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        backspaceBtn.setOnAction(e -> handleBackspace());
        grid.add(backspaceBtn, 2, 3);

        return grid;
    }

    private Button createKeypadButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("keypad-button");
        btn.setOnAction(e -> handleDigit(text));
        return btn;
    }

    private void handleDigit(String digit) {
        if (currentPin.length() < PIN_LENGTH) {
            currentPin.append(digit);
            updateDots();

            if (currentPin.length() == PIN_LENGTH) {
                // Small delay to show the last dot filled before checking
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(100), event -> checkPin()));
                timeline.play();
            }
        }
    }

    private void handleBackspace() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updateDots();
            messageLabel.setText("");
        }
    }

    private void updateDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            Circle dot = pinDots.get(i);
            if (i < currentPin.length()) {
                if (!dot.getStyleClass().contains("pin-dot-filled")) {
                    dot.getStyleClass().add("pin-dot-filled");

                    // Add pop animation effect
                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), dot);
                    scale.setFromX(1.0);
                    scale.setFromY(1.0);
                    scale.setToX(1.5);
                    scale.setToY(1.5);
                    scale.setAutoReverse(true);
                    scale.setCycleCount(2);
                    scale.play();
                }
            } else {
                dot.getStyleClass().remove("pin-dot-filled");
            }
        }
    }

    private void checkPin() {
        String pin = currentPin.toString();

        try {
            if (SecurityContext.getInstance().authenticate(pin)) {
                // Success
                messageLabel.setStyle("-fx-text-fill: -roam-green;");
                messageLabel.setText("✓ Unlocked");

                // Small delay before unlocking for UX
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(300), event -> onUnlock.run()));
                timeline.play();

            } else {
                // Failed authentication
                showError("Incorrect PIN");
                shakeAnimation();

                // Clear PIN after delay
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(500), event -> {
                            currentPin.setLength(0);
                            updateDots();
                        }));
                timeline.play();
            }
        } catch (SecurityException e) {
            // Rate limit exceeded
            showError(e.getMessage());
            setDisable(true);

            // Re-enable after 60 seconds
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(60), event -> {
                        setDisable(false);
                        messageLabel.setText("");
                        currentPin.setLength(0);
                        updateDots();
                    }));
            timeline.play();
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: -roam-red;");
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
