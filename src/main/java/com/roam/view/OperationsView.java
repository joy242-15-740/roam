package com.roam.view;

import com.roam.controller.OperationsController;
import com.roam.model.Operation;
import com.roam.view.components.OperationTableView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;

public class OperationsView extends VBox {

    private final OperationsController controller;
    private final OperationTableView tableView;
    private final StackPane contentArea;
    private final ScrollPane scrollPane;
    private final VBox emptyState;

    private Consumer<Operation> onOperationClick;

    public OperationsView(OperationsController controller) {
        this.controller = controller;
        this.tableView = new OperationTableView();
        this.contentArea = new StackPane();
        this.scrollPane = new ScrollPane();
        this.emptyState = createEmptyState();

        initialize();
    }

    public void setOnOperationClick(Consumer<Operation> handler) {
        this.onOperationClick = handler;
        tableView.setOnOperationClick(handler);
    }

    private void initialize() {
        // Configure container
        setStyle("-fx-background-color: -roam-bg-primary;");
        setSpacing(0);

        // Create header
        HBox header = createHeader();

        // Configure scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: -roam-bg-primary; -fx-background: -roam-bg-primary;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Configure content area
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: -roam-bg-primary;");

        // Set content area in scroll pane
        scrollPane.setContent(contentArea);

        // Configure table
        controller.setTableView(tableView);
        controller.setOnDataChanged(this::updateContent);

        tableView.setOnEdit(controller::editOperation);
        tableView.setOnDelete(controller::deleteOperation);

        // Add components
        getChildren().addAll(header, scrollPane);

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
            // Configure table for scrollable content
            tableView.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(tableView, Priority.ALWAYS);
            contentArea.getChildren().add(tableView);
        }
    }
}
