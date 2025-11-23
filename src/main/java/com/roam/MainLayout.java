package com.roam;

import com.roam.model.Operation;
import com.roam.service.SearchService;
import com.roam.view.SearchView;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

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
    private HBox searchBar;
    private TextField searchField;
    private Region resizeHandle;

    // Sidebar dimensions
    private static final double SIDEBAR_WIDTH_EXPANDED = 240;
    private static final double SIDEBAR_WIDTH_COLLAPSED = 60;
    private static final double SIDEBAR_MIN_WIDTH = 180;
    private static final double SIDEBAR_MAX_WIDTH = 400;
    private static final double RESIZE_HANDLE_WIDTH = 5;

    // Resize state
    private double dragStartX;
    private double dragStartWidth;

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

        // Create resize handle
        resizeHandle = createResizeHandle();

        // Create sidebar container with resize handle
        HBox sidebarContainer = new HBox();
        sidebarContainer.getChildren().addAll(sidebar, resizeHandle);

        // Create content area
        contentArea = createContentArea();

        // Set layout regions
        setLeft(sidebarContainer);
        setCenter(contentArea);
    }

    private Region createResizeHandle() {
        Region handle = new Region();
        handle.setPrefWidth(RESIZE_HANDLE_WIDTH);
        handle.setMinWidth(RESIZE_HANDLE_WIDTH);
        handle.setMaxWidth(RESIZE_HANDLE_WIDTH);
        handle.setStyle("-fx-background-color: transparent; -fx-cursor: h-resize;");

        // Change appearance on hover
        handle.setOnMouseEntered(e -> {
            handle.setCursor(Cursor.H_RESIZE);
            handle.setStyle("-fx-background-color: rgba(66, 133, 244, 0.3); -fx-cursor: h-resize;");
        });
        handle.setOnMouseExited(e -> {
            handle.setCursor(Cursor.DEFAULT);
            handle.setStyle("-fx-background-color: transparent; -fx-cursor: h-resize;");
        });

        // Handle drag to resize
        handle.setOnMousePressed(this::handleResizeStart);
        handle.setOnMouseDragged(this::handleResizeDrag);

        return handle;
    }

    private void handleResizeStart(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartWidth = sidebar.getWidth();
    }

    private void handleResizeDrag(MouseEvent event) {
        if (sidebarCollapsed)
            return; // Don't resize when collapsed

        double deltaX = event.getSceneX() - dragStartX;
        double newWidth = dragStartWidth + deltaX;

        // Clamp to min/max width
        newWidth = Math.max(SIDEBAR_MIN_WIDTH, Math.min(SIDEBAR_MAX_WIDTH, newWidth));

        // Update sidebar width
        sidebar.setPrefWidth(newWidth);
        sidebar.setMinWidth(newWidth);
        sidebar.setMaxWidth(newWidth);

        // Update button widths proportionally
        double buttonWidth = newWidth - 40; // Account for padding
        operationsBtn.setPrefWidth(buttonWidth);
        calendarBtn.setPrefWidth(buttonWidth);
        tasksBtn.setPrefWidth(buttonWidth);
        wikiBtn.setPrefWidth(buttonWidth);
        journalBtn.setPrefWidth(buttonWidth);
        statisticsBtn.setPrefWidth(buttonWidth);
        settingsBtn.setPrefWidth(buttonWidth);
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
        toggleButton = new Button();
        toggleButton.setGraphic(new FontIcon(Feather.MENU));
        toggleButton.getStyleClass().add("icon-button");
        toggleButton.setPrefSize(30, 30);
        toggleButton.setOnAction(e -> toggleSidebar());

        header.getChildren().addAll(titleLabel, toggleButton);

        // Search bar
        searchBar = createSearchBar();

        // Navigation buttons
        operationsBtn = createNavButton("Operations", Feather.CLIPBOARD, true);
        calendarBtn = createNavButton("Calendar", Feather.CALENDAR, false);
        tasksBtn = createNavButton("Tasks", Feather.CHECK_SQUARE, false);
        wikiBtn = createNavButton("Wiki", Feather.FILE_TEXT, false);
        journalBtn = createNavButton("Journal", Feather.BOOK, false);
        statisticsBtn = createNavButton("Statistics", Feather.BAR_CHART_2, false);
        settingsBtn = createNavButton("Settings", Feather.SETTINGS, false);

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
                searchBar,
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

    private HBox createSearchBar() {
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(0, 0, 10, 0));
        searchContainer.getStyleClass().add("search-bar");

        // Search icon
        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(16);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefHeight(35);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Handle Enter key to perform search
        searchField.setOnAction(e -> performSearch());

        // Search button
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("button-primary");
        searchBtn.setPrefHeight(35);
        searchBtn.setOnAction(e -> performSearch());

        searchContainer.getChildren().addAll(searchIcon, searchField, searchBtn);
        return searchContainer;
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        try {
            SearchService searchService = SearchService.getInstance();
            SearchService.SearchFilter filter = new SearchService.SearchFilter();
            List<SearchService.SearchResult> results = searchService.search(query, filter);

            // Store the current view so we can return to it
            Node currentView = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);

            // Show results in SearchView
            SearchView searchView = new SearchView(results, query);
            searchView.setOnResultSelected(this::navigateToSearchResult);
            searchView.setOnBackAction(() -> {
                if (currentView != null) {
                    showViewWithTransition(currentView);
                } else {
                    // Default to wiki view if no previous view
                    switchView("wiki");
                }
            });

            showViewWithTransition(searchView);

        } catch (Exception ex) {
            ex.printStackTrace();
            // TODO: Show error dialog
        }
    }

    private void navigateToSearchResult(SearchService.SearchResult result) {
        // Navigate to the appropriate view based on result type
        switch (result.type) {
            case "wiki":
                switchView("wiki");
                // TODO: Navigate to specific wiki
                break;
            case "task":
                switchView("tasks");
                // TODO: Navigate to specific task
                break;
            case "operation":
                switchView("operations");
                // TODO: Navigate to specific operation
                break;
            case "event":
                switchView("calendar");
                // TODO: Navigate to specific event
                break;
            case "journal":
                switchView("journal");
                // TODO: Navigate to specific journal entry
                break;
        }
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
        sidebar.setPrefWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setPadding(new Insets(20, 5, 20, 5));

        // Hide resize handle when collapsed
        resizeHandle.setVisible(false);
        resizeHandle.setManaged(false);

        // Hide text elements
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);

        // Hide search bar
        searchBar.setVisible(false);
        searchBar.setManaged(false);

        // Update button to show only icons
        operationsBtn.setText("");
        calendarBtn.setText("");
        tasksBtn.setText("");
        wikiBtn.setText("");
        journalBtn.setText("");
        statisticsBtn.setText("");
        settingsBtn.setText("");

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
    }

    private void expandSidebar() {
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setPadding(new Insets(20));

        // Show resize handle when expanded
        resizeHandle.setVisible(true);
        resizeHandle.setManaged(true);

        // Show text elements
        titleLabel.setVisible(true);
        titleLabel.setManaged(true);

        // Show search bar
        searchBar.setVisible(true);
        searchBar.setManaged(true);

        // Restore full button text
        operationsBtn.setText("Operations");
        calendarBtn.setText("Calendar");
        tasksBtn.setText("Tasks");
        wikiBtn.setText("Wiki");
        journalBtn.setText("Journal");
        statisticsBtn.setText("Statistics");
        settingsBtn.setText("Settings");

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
    }

    private Button createNavButton(String text, Feather iconType, boolean active) {
        Button button = new Button(text);
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(18);
        button.setGraphic(icon);
        button.getStyleClass().add("nav-button");
        button.setPrefWidth(200);
        button.setPrefHeight(45);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(10);

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
