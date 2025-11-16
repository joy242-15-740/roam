package com.roam.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

public class Breadcrumb extends HBox {

    private final Runnable onNavigateBack;

    public Breadcrumb(String operationName, Runnable onNavigateBack) {
        this.onNavigateBack = onNavigateBack;

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(50);
        setPadding(new Insets(0, 20, 0, 20));
        setStyle("-fx-background-color: #FFFFFF;");

        // "Operations" link
        Hyperlink operationsLink = new Hyperlink("Operations");
        operationsLink.setFont(Font.font("Poppins Regular", 14));
        operationsLink.setStyle("-fx-text-fill: #4285f4; -fx-border-width: 0; -fx-underline: false;");
        operationsLink.setOnMouseEntered(
                e -> operationsLink.setStyle("-fx-text-fill: #4285f4; -fx-border-width: 0; -fx-underline: true;"));
        operationsLink.setOnMouseExited(
                e -> operationsLink.setStyle("-fx-text-fill: #4285f4; -fx-border-width: 0; -fx-underline: false;"));
        operationsLink.setOnAction(e -> {
            if (onNavigateBack != null) {
                onNavigateBack.run();
            }
        });

        // Separator
        Label separator = new Label(">");
        separator.setFont(Font.font("Poppins Regular", 14));
        separator.setStyle("-fx-text-fill: #9E9E9E;");

        // Operation name
        String displayName = operationName.length() > 40
                ? operationName.substring(0, 40) + "..."
                : operationName;
        Label nameLabel = new Label(displayName);
        nameLabel.setFont(Font.font("Poppins Regular", 14));
        nameLabel.setStyle("-fx-text-fill: #000000;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Back button
        Hyperlink backButton = new Hyperlink("← Back");
        backButton.setFont(Font.font("Poppins Regular", 14));
        backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #616161; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;");
        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: #F5F5F5; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;"));
        backButton.setOnMouseExited(e -> backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #616161; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;"));
        backButton.setOnAction(e -> {
            if (onNavigateBack != null) {
                onNavigateBack.run();
            }
        });

        getChildren().addAll(operationsLink, separator, nameLabel, spacer, backButton);
    }
}
