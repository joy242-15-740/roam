package com.roam.view;

import com.roam.controller.OperationsController;
import com.roam.model.Operation;
import com.roam.view.components.OperationTableView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;

public class OperationsView extends StackPane {

    private final OperationsController controller;
    private final OperationTableView tableView;
    private final StackPane contentArea;
    private final VBox emptyState;
    private final VBox contentPane;

    public OperationsView(OperationsController controller) {
        this.controller = controller;
        this.tableView = new OperationTableView();
        this.contentArea = new StackPane();
        this.emptyState = createEmptyState();
        this.contentPane = new VBox();
        getChildren().add(contentPane);

        initialize();

        // Add listeners for responsive scaling
        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleContent());
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleContent());
    }

    private void scaleContent() {
        double width = getWidth();
        double height = getHeight();

        // Use layout bounds to get the actual size of the content
        double contentWidth = contentPane.getLayoutBounds().getWidth();
        double contentHeight = contentPane.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        // Calculate scale factors
        double scaleX = width < contentWidth ? width / contentWidth : 1.0;
        double scaleY = height < contentHeight ? height / contentHeight : 1.0;

        // Use the smaller scale to maintain aspect ratio and fit within bounds
        double scale = Math.min(scaleX, scaleY);

        contentPane.setScaleX(scale);
        contentPane.setScaleY(scale);
    }

    public void setOnOperationClick(Consumer<Operation> handler) {
        tableView.setOnOperationClick(handler);
    }

    private void initialize() {
        // Configure container
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");
        contentPane.setSpacing(0);

        // Create header
        HBox header = createHeader();

        // Configure content area
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: -roam-bg-primary;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Configure table
        controller.setTableView(tableView);
        controller.setOnDataChanged(this::updateContent);

        tableView.setOnEdit(controller::editOperation);
        tableView.setOnDelete(controller::deleteOperation);

        // Add components
        contentPane.getChildren().addAll(header, contentArea);

        // Initial load
        loadData();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPrefHeight(60);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: -roam-bg-primary;");

        // Title
        Label title = new Label("Operations");
        title.setFont(Font.font("Poppins Bold", 28));
        title.setStyle("-fx-text-fill: -roam-text-primary;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // New Operation button
        Button newBtn = createNewOperationButton();

        header.getChildren().addAll(title, spacer, newBtn);
        return header;
    }

    private Button createNewOperationButton() {
        Button btn = new Button("+ New Operation");
        btn.setFont(Font.font("Poppins Regular", 14));
        btn.setMinWidth(150);
        btn.setPrefHeight(40);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: -roam-blue-hover; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        btn.setOnAction(e -> controller.createOperation());

        return btn;
    }

    private VBox createEmptyState() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);

        // Icon
        FontIcon icon = new FontIcon(Feather.CLIPBOARD);
        icon.setIconSize(72);
        icon.setStyle("-fx-icon-color: -roam-text-secondary;");

        // Title
        Label title = new Label("No Operations Yet");
        title.setFont(Font.font("Poppins Bold", 24));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        // Description
        Label description = new Label("Click '+ New Operation' above to create your first operation");
        description.setFont(Font.font("Poppins Regular", 16));
        description.setStyle("-fx-text-fill: -roam-text-hint;");

        container.getChildren().addAll(icon, title, description);
        return container;
    }

    private void loadData() {
        controller.refreshTable();
    }

    private void updateContent() {
        List<Operation> operations = controller.loadOperations();

        contentArea.getChildren().clear();

        if (operations.isEmpty()) {
            contentArea.getChildren().add(emptyState);
            // Center empty state in available space
            StackPane.setAlignment(emptyState, Pos.CENTER);
        } else {
            // Add table to content area
            contentArea.getChildren().add(tableView);
        }
    }
}
