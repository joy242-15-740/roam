package com.roam;

import com.roam.model.Operation;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private Button journalBtn;
    private Button statisticsBtn;
    private Button settingsBtn;

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

    // Current navigation state
    private Operation currentOperation;

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
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);

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
        toggleButton.getStyleClass().add("icon-button");
        toggleButton.setPrefSize(30, 30);
        toggleButton.setOnAction(e -> toggleSidebar());

        header.getChildren().addAll(titleLabel, toggleButton);

        // Navigation buttons
        operationsBtn = createNavButton("📋 Operations", true);
        calendarBtn = createNavButton("📅 Calendar", false);
        tasksBtn = createNavButton("✓ Tasks", false);
        wikiBtn = createNavButton("📝 Wiki", false);
        journalBtn = createNavButton("📓 Journal", false);
        statisticsBtn = createNavButton("📊 Statistics", false);
        settingsBtn = createNavButton("⚙ Settings", false);

        // Set button actions
        operationsBtn.setOnAction(e -> switchView("operations"));
        calendarBtn.setOnAction(e -> switchView("calendar"));
        tasksBtn.setOnAction(e -> switchView("tasks"));
        wikiBtn.setOnAction(e -> switchView("wiki"));
        journalBtn.setOnAction(e -> switchView("journal"));
        statisticsBtn.setOnAction(e -> switchView("statistics"));
        settingsBtn.setOnAction(e -> switchView("settings"));

        // Spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(
                header,
                operationsBtn,
                calendarBtn,
                tasksBtn,
                wikiBtn,
                journalBtn,
                statisticsBtn,
                settingsBtn,
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
        operationsBtn.setText("📋");
        calendarBtn.setText("📅");
        tasksBtn.setText("✓");
        wikiBtn.setText("📝");
        journalBtn.setText("📓");
        statisticsBtn.setText("📊");
        settingsBtn.setText("⚙");

        // Center align buttons
        operationsBtn.setAlignment(Pos.CENTER);
        calendarBtn.setAlignment(Pos.CENTER);
        tasksBtn.setAlignment(Pos.CENTER);
        wikiBtn.setAlignment(Pos.CENTER);
        journalBtn.setAlignment(Pos.CENTER);
        statisticsBtn.setAlignment(Pos.CENTER);
        settingsBtn.setAlignment(Pos.CENTER);

        // Adjust button widths
        operationsBtn.setPrefWidth(50);
        calendarBtn.setPrefWidth(50);
        tasksBtn.setPrefWidth(50);
        wikiBtn.setPrefWidth(50);
        journalBtn.setPrefWidth(50);
        statisticsBtn.setPrefWidth(50);
        settingsBtn.setPrefWidth(50);

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
        operationsBtn.setText("📋 Operations");
        calendarBtn.setText("📅 Calendar");
        tasksBtn.setText("✓ Tasks");
        wikiBtn.setText("📝 Wiki");
        journalBtn.setText("📓 Journal");
        statisticsBtn.setText("📊 Statistics");
        settingsBtn.setText("⚙ Settings");

        // Left align buttons
        operationsBtn.setAlignment(Pos.CENTER_LEFT);
        calendarBtn.setAlignment(Pos.CENTER_LEFT);
        tasksBtn.setAlignment(Pos.CENTER_LEFT);
        wikiBtn.setAlignment(Pos.CENTER_LEFT);
        journalBtn.setAlignment(Pos.CENTER_LEFT);
        statisticsBtn.setAlignment(Pos.CENTER_LEFT);
        settingsBtn.setAlignment(Pos.CENTER_LEFT);

        // Restore button widths
        operationsBtn.setPrefWidth(200);
        calendarBtn.setPrefWidth(200);
        tasksBtn.setPrefWidth(200);
        wikiBtn.setPrefWidth(200);
        journalBtn.setPrefWidth(200);
        statisticsBtn.setPrefWidth(200);
        settingsBtn.setPrefWidth(200);

        // Update toggle button icon
        toggleButton.setText("☰");
    }

    private Button createNavButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setPrefWidth(200);
        button.setPrefHeight(45);
        button.setAlignment(Pos.CENTER_LEFT);

        // Apply initial state
        updateButtonState(button, active);

        return button;
    }

    private void updateButtonState(Button button, boolean active) {
        if (active) {
            button.getStyleClass().remove("nav-button");
            button.getStyleClass().add("nav-button");
            button.getStyleClass().add("nav-button:selected");
            button.setStyle("-fx-background-color: #4285f4; -fx-text-fill: #FFFFFF; -fx-font-family: 'Poppins Bold';");
        } else {
            button.getStyleClass().removeAll("nav-button:selected");
            button.setStyle("-fx-font-family: 'Poppins';");
        }
    }

    private boolean isButtonActive(Button button) {
        return button.getStyle().contains("#4285f4");
    }

    private StackPane createContentArea() {
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: -roam-bg-primary;");
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
        updateButtonState(journalBtn, false);
        updateButtonState(statisticsBtn, false);
        updateButtonState(settingsBtn, false);

        // Clear current operation when switching main views
        currentOperation = null;

        // Activate selected button and show corresponding view
        Node newView = null;
        switch (viewName) {
            case "operations":
                updateButtonState(operationsBtn, true);
                com.roam.controller.OperationsController opsController = new com.roam.controller.OperationsController();
                com.roam.view.OperationsView operationsView = new com.roam.view.OperationsView(opsController);
                operationsView.setOnOperationClick(this::showOperationDetail);
                newView = operationsView;
                break;
            case "calendar":
                updateButtonState(calendarBtn, true);
                com.roam.controller.CalendarController calController = new com.roam.controller.CalendarController();
                newView = new com.roam.view.CalendarView(calController);
                break;
            case "tasks":
                updateButtonState(tasksBtn, true);
                com.roam.controller.TasksController tasksController = new com.roam.controller.TasksController();
                newView = new com.roam.view.TasksView(tasksController);
                break;
            case "wiki":
                updateButtonState(wikiBtn, true);
                com.roam.controller.WikiController wikiController = new com.roam.controller.WikiController();
                newView = new com.roam.view.WikiView(wikiController, regularFont, boldFont);
                contentArea.setPadding(new Insets(0)); // Remove padding for wiki view
                break;
            case "journal":
                updateButtonState(journalBtn, true);
                newView = new com.roam.view.JournalView();
                contentArea.setPadding(new Insets(0));
                break;
            case "statistics":
                updateButtonState(statisticsBtn, true);
                com.roam.controller.WikiController statsController = new com.roam.controller.WikiController();
                newView = new com.roam.view.StatisticsView(statsController);
                contentArea.setPadding(new Insets(30));
                break;
            case "settings":
                updateButtonState(settingsBtn, true);
                newView = new com.roam.view.SettingsView();
                break;
        }

        if (newView != null) {
            showViewWithTransition(newView);
        }
    }

    private void showViewWithTransition(Node view) {
        // Clear content area
        contentArea.getChildren().clear();

        // Set initial opacity to 0 for fade in effect
        view.setOpacity(0);
        contentArea.getChildren().add(view);

        // Create and play fade transition
        FadeTransition ft = new FadeTransition(Duration.millis(300), view);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showOperationsView() {
        com.roam.controller.OperationsController controller = new com.roam.controller.OperationsController();
        com.roam.view.OperationsView operationsView = new com.roam.view.OperationsView(controller);

        // Set click handler to navigate to operation detail
        operationsView.setOnOperationClick(this::showOperationDetail);

        showViewWithTransition(operationsView);
    }

    public void showOperationDetail(Operation operation) {
        currentOperation = operation;

        // Keep operations button active
        updateButtonState(operationsBtn, true);
        updateButtonState(calendarBtn, false);
        updateButtonState(tasksBtn, false);
        updateButtonState(wikiBtn, false);

        // Clear and show detail view
        contentArea.setPadding(new Insets(0)); // Remove padding for detail view

        com.roam.view.OperationDetailView detailView = new com.roam.view.OperationDetailView(
                operation,
                this::navigateBackToOperations);

        showViewWithTransition(detailView);
    }

    private void navigateBackToOperations() {
        currentOperation = null;
        contentArea.setPadding(new Insets(30)); // Restore padding
        switchView("operations");
    }

    private void showCalendarView() {
        com.roam.controller.CalendarController controller = new com.roam.controller.CalendarController();
        com.roam.view.CalendarView calendarView = new com.roam.view.CalendarView(controller);
        showViewWithTransition(calendarView);
    }

    private void showTasksView() {
        com.roam.controller.TasksController controller = new com.roam.controller.TasksController();
        com.roam.view.TasksView tasksView = new com.roam.view.TasksView(controller);
        showViewWithTransition(tasksView);
    }

    private void showWikiView() {
        com.roam.controller.WikiController controller = new com.roam.controller.WikiController();
        com.roam.view.WikiView wikiView = new com.roam.view.WikiView(controller, regularFont, boldFont);
        contentArea.setPadding(new Insets(0)); // Remove padding for wiki view
        showViewWithTransition(wikiView);
    }

    private Label createPlaceholder(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Poppins Regular", 18));
        label.setStyle("-fx-text-fill: #000000;");
        return label;
    }
}
