package com.roam;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class MainLayout extends BorderPane {

    private final Font regularFont;
    private final Font boldFont;

    // Navigation buttons
    private Button operationsBtn;
    private Button calendarBtn;
    private Button tasksBtn;
    private Button wikiBtn;

    // Sidebar components
    private VBox sidebar;
    private Button toggleButton;
    private Label titleLabel;
    private boolean sidebarCollapsed = false;

    // Sidebar dimensions
    private static final double SIDEBAR_WIDTH_EXPANDED = 240;
    private static final double SIDEBAR_WIDTH_COLLAPSED = 60;

    // Content area
    private StackPane contentArea;

    public MainLayout(Font regularFont, Font boldFont) {
        this.regularFont = regularFont;
        this.boldFont = boldFont;

        initializeLayout();
    }

    private void initializeLayout() {
        // Create sidebar
        sidebar = createSidebar();

        // Create content area
        contentArea = createContentArea();

        // Set layout regions
        setLeft(sidebar);
        setCenter(contentArea);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #F5F5F5;");

        // Header container with title and toggle button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // App title
        titleLabel = new Label("Roam");
        titleLabel.setFont(Font.font("Poppins Bold", 24));
        titleLabel.setStyle("-fx-text-fill: #000000;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Toggle button
        toggleButton = new Button("☰");
        toggleButton.setPrefSize(30, 30);
        toggleButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 5;");
        toggleButton.setOnMouseEntered(e -> toggleButton.setStyle(
                "-fx-background-color: #FFFFFF;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 5;"));
        toggleButton.setOnMouseExited(e -> toggleButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #000000;" +
                        "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 5;"));
        toggleButton.setOnAction(e -> toggleSidebar());

        header.getChildren().addAll(titleLabel, toggleButton);

        // Navigation buttons
        operationsBtn = createNavButton("Operations", true);
        calendarBtn = createNavButton("Calendar", false);
        tasksBtn = createNavButton("Tasks", false);
        wikiBtn = createNavButton("Wiki", false);

        // Set button actions
        operationsBtn.setOnAction(e -> switchView("operations"));
        calendarBtn.setOnAction(e -> switchView("calendar"));
        tasksBtn.setOnAction(e -> switchView("tasks"));
        wikiBtn.setOnAction(e -> switchView("wiki"));

        // Spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                header,
                operationsBtn,
                calendarBtn,
                tasksBtn,
                wikiBtn,
                spacer);

        return sidebar;
    }

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;

        if (sidebarCollapsed) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }

    private void collapseSidebar() {
        // Animate width change
        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebar);

        sidebar.setPrefWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setPadding(new Insets(20, 5, 20, 5));

        // Hide text elements
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);

        // Update button text to show only icons
        operationsBtn.setText(" ");
        calendarBtn.setText(" ");
        tasksBtn.setText(" ");
        wikiBtn.setText(" ");

        // Center align buttons
        operationsBtn.setAlignment(Pos.CENTER);
        calendarBtn.setAlignment(Pos.CENTER);
        tasksBtn.setAlignment(Pos.CENTER);
        wikiBtn.setAlignment(Pos.CENTER);

        // Adjust button widths
        operationsBtn.setPrefWidth(50);
        calendarBtn.setPrefWidth(50);
        tasksBtn.setPrefWidth(50);
        wikiBtn.setPrefWidth(50);

        // Update toggle button icon
        toggleButton.setText("☰");
    }

    private void expandSidebar() {
        // Animate width change
        TranslateTransition transition = new TranslateTransition(Duration.millis(250), sidebar);

        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setPadding(new Insets(20));

        // Show text elements
        titleLabel.setVisible(true);
        titleLabel.setManaged(true);

        // Restore full button text
        operationsBtn.setText("Operations");
        calendarBtn.setText("Calendar");
        tasksBtn.setText("Tasks");
        wikiBtn.setText("Wiki");

        // Left align buttons
        operationsBtn.setAlignment(Pos.CENTER_LEFT);
        calendarBtn.setAlignment(Pos.CENTER_LEFT);
        tasksBtn.setAlignment(Pos.CENTER_LEFT);
        wikiBtn.setAlignment(Pos.CENTER_LEFT);

        // Restore button widths
        operationsBtn.setPrefWidth(200);
        calendarBtn.setPrefWidth(200);
        tasksBtn.setPrefWidth(200);
        wikiBtn.setPrefWidth(200);

        // Update toggle button icon
        toggleButton.setText("☰");
    }

    private Button createNavButton(String text, boolean active) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(45);
        button.setPadding(new Insets(10, 15, 10, 15));
        button.setAlignment(Pos.CENTER_LEFT);

        // Apply initial state
        updateButtonState(button, active);

        return button;
    }

    private void updateButtonState(Button button, boolean active) {
        if (active) {
            // Active state: blue background, white text, no hover
            button.setStyle(
                    "-fx-background-color: #4285f4;" +
                            "-fx-text-fill: #FFFFFF;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-family: 'Poppins Regular';" +
                            "-fx-font-size: 14px;" +
                            "-fx-alignment: CENTER-LEFT;" +
                            "-fx-cursor: hand;");
            // Remove hover effect for active button
            button.setOnMouseEntered(null);
            button.setOnMouseExited(null);
        } else {
            // Inactive state: transparent background, black text
            button.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #000000;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-family: 'Poppins Regular';" +
                            "-fx-font-size: 14px;" +
                            "-fx-alignment: CENTER-LEFT;" +
                            "-fx-cursor: hand;");
            // Add hover effect for inactive buttons
            button.setOnMouseEntered(e -> {
                if (!isButtonActive(button)) {
                    button.setStyle(
                            "-fx-background-color: #FFFFFF;" +
                                    "-fx-text-fill: #000000;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-font-family: 'Poppins Regular';" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-alignment: CENTER-LEFT;" +
                                    "-fx-cursor: hand;");
                }
            });
            button.setOnMouseExited(e -> {
                if (!isButtonActive(button)) {
                    button.setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-text-fill: #000000;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-font-family: 'Poppins Regular';" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-alignment: CENTER-LEFT;" +
                                    "-fx-cursor: hand;");
                }
            });
        }
    }

    private boolean isButtonActive(Button button) {
        return button.getStyle().contains("#4285f4");
    }

    private StackPane createContentArea() {
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #FFFFFF;");
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);

        // Assign contentArea first
        contentArea = content;

        // Show default view (Operations)
        showOperationsView();

        return content;
    }

    private void switchView(String viewName) {
        // Deactivate all buttons
        updateButtonState(operationsBtn, false);
        updateButtonState(calendarBtn, false);
        updateButtonState(tasksBtn, false);
        updateButtonState(wikiBtn, false);

        // Clear content area
        contentArea.getChildren().clear();

        // Activate selected button and show corresponding view
        switch (viewName) {
            case "operations":
                updateButtonState(operationsBtn, true);
                showOperationsView();
                break;
            case "calendar":
                updateButtonState(calendarBtn, true);
                showCalendarView();
                break;
            case "tasks":
                updateButtonState(tasksBtn, true);
                showTasksView();
                break;
            case "wiki":
                updateButtonState(wikiBtn, true);
                showWikiView();
                break;
        }
    }

    private void showOperationsView() {
        com.roam.controller.OperationsController controller = new com.roam.controller.OperationsController();
        com.roam.view.OperationsView operationsView = new com.roam.view.OperationsView(controller);
        contentArea.getChildren().add(operationsView);
    }

    private void showCalendarView() {
        Label placeholder = createPlaceholder("Coming Soon");
        contentArea.getChildren().add(placeholder);
    }

    private void showTasksView() {
        Label placeholder = createPlaceholder("Coming Soon");
        contentArea.getChildren().add(placeholder);
    }

    private void showWikiView() {
        Label placeholder = createPlaceholder("Coming Soon");
        contentArea.getChildren().add(placeholder);
    }

    private Label createPlaceholder(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Poppins Regular", 18));
        label.setStyle("-fx-text-fill: #000000;");
        return label;
    }
}
