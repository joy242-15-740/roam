package com.roam.layout;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents the sidebar component with navigation buttons and search
 * functionality.
 * Manages sidebar creation, button states, collapse/expand animations, and
 * search bar.
 */
public class SidebarComponent {

    private static final Logger logger = LoggerFactory.getLogger(SidebarComponent.class);

    // Sidebar dimensions
    private static final double SIDEBAR_WIDTH_EXPANDED = 240;
    private static final double SIDEBAR_WIDTH_COLLAPSED = 60;

    private final VBox sidebar;
    private final Map<String, Button> navigationButtons;
    private TextField searchField;
    private final Label titleLabel;
    private final HBox searchBar;
    private final Region resizeHandle;

    private boolean collapsed = false;

    // Callbacks
    private final Consumer<String> onNavigate;
    private final Consumer<String> onSearch;
    private final Runnable onToggle;

    /**
     * Creates a new sidebar component.
     * 
     * @param onNavigate   Callback when navigation button is clicked (receives view
     *                     type)
     * @param onSearch     Callback when search is performed (receives search query)
     * @param onToggle     Callback when sidebar toggle button is clicked
     * @param resizeHandle The resize handle to show/hide based on collapsed state
     */
    public SidebarComponent(Consumer<String> onNavigate, Consumer<String> onSearch,
            Runnable onToggle, Region resizeHandle) {
        this.onNavigate = onNavigate;
        this.onSearch = onSearch;
        this.onToggle = onToggle;
        this.resizeHandle = resizeHandle;
        this.navigationButtons = new HashMap<>();

        // Create sidebar UI components
        sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);

        // Create header with title and toggle button
        HBox header = createHeader();
        // Extract title label from header for later reference
        titleLabel = (Label) header.getChildren().get(0);

        // Create search bar
        searchBar = createSearchBar();
        // searchField is initialized in createSearchBar

        // Create navigation buttons
        Button operationsBtn = createNavButton("Operations", Feather.CLIPBOARD, "operations", true);
        Button calendarBtn = createNavButton("Calendar", Feather.CALENDAR, "calendar", false);
        Button tasksBtn = createNavButton("Tasks", Feather.CHECK_SQUARE, "tasks", false);
        Button wikiBtn = createNavButton("Wiki", Feather.FILE_TEXT, "wiki", false);
        Button journalBtn = createNavButton("Journal", Feather.BOOK, "journal", false);
        Button statisticsBtn = createNavButton("Statistics", Feather.BAR_CHART_2, "statistics", false);
        Button settingsBtn = createSettingsButton();

        // Store references
        navigationButtons.put("operations", operationsBtn);
        navigationButtons.put("calendar", calendarBtn);
        navigationButtons.put("tasks", tasksBtn);
        navigationButtons.put("wiki", wikiBtn);
        navigationButtons.put("journal", journalBtn);
        navigationButtons.put("statistics", statisticsBtn);
        // Settings button is excluded from navigationButtons to maintain its custom
        // style during expand/collapse

        // Spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Assemble sidebar
        sidebar.getChildren().addAll(
                header,
                searchBar,
                operationsBtn,
                calendarBtn,
                tasksBtn,
                wikiBtn,
                journalBtn,
                statisticsBtn,
                spacer,
                settingsBtn);

        logger.debug("Sidebar component created");
    }

    /**
     * Creates the sidebar header with app title and toggle button.
     */
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 20, 0));

        // App title
        Label title = new Label("Roam");
        title.setFont(Font.font("Poppins Bold", 24));
        title.setStyle("-fx-text-fill: #000000;");
        HBox.setHgrow(title, Priority.ALWAYS);

        // Toggle button
        Button toggleButton = new Button();
        toggleButton.setGraphic(new FontIcon(Feather.MENU));
        toggleButton.getStyleClass().add("icon-button");
        toggleButton.setPrefSize(30, 30);
        toggleButton.setOnAction(e -> {
            toggle();
            if (onToggle != null) {
                onToggle.run();
            }
        });

        header.getChildren().addAll(title, toggleButton);
        return header;
    }

    /**
     * Creates the search bar with text field and search button.
     */
    private HBox createSearchBar() {
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.setPadding(new Insets(0, 0, 10, 0));
        searchContainer.getStyleClass().add("search-bar");

        // Search field
        TextField field = new TextField();
        field.setPromptText("Search...");
        field.setPrefHeight(40);
        field.setFont(Font.font("Poppins Regular", 14));
        field.setStyle(
                "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 10 20 10 20;");

        field.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                field.setStyle(
                        "-fx-border-color: -roam-blue; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-radius: 20; " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 10 20 10 20;");
            } else {
                field.setStyle(
                        "-fx-border-color: -roam-border; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 20; " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 10 20 10 20;");
            }
        });

        HBox.setHgrow(field, Priority.ALWAYS);

        // Handle Enter key to perform search
        field.setOnAction(e -> performSearch());

        // Search button (optional, maybe remove if field is enough? User said "same
        // styled task", TasksToolbar had no button)
        // But SidebarComponent had a button. I'll keep the button but maybe style it or
        // hide it?
        // The user said "implement same styled task". TasksToolbar only had a text
        // field.
        // I will remove the button to match TasksToolbar style more closely, or keep it
        // if needed.
        // Sidebar usually needs a button or icon.
        // I'll keep the button but maybe make it an icon button inside the field?
        // For now, I'll just style the field as requested. I'll remove the button to
        // match TasksToolbar.

        searchContainer.getChildren().addAll(field);

        // Update reference
        searchField = field;

        return searchContainer;
    }

    /**
     * Performs search with current query.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty() && onSearch != null) {
            logger.debug("Search triggered: {}", query);
            onSearch.accept(query);
        }
    }

    /**
     * Creates the settings button with custom style.
     */
    private Button createSettingsButton() {
        Button btn = new Button();
        btn.setGraphic(new FontIcon(Feather.SETTINGS));
        btn.setPrefSize(40, 40);
        btn.setFont(Font.font(18));
        btn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));

        btn.setOnAction(e -> {
            if (onNavigate != null) {
                logger.debug("Navigation button clicked: settings");
                onNavigate.accept("settings");
            }
        });

        return btn;
    }

    /**
     * Creates a navigation button.
     */
    private Button createNavButton(String text, Feather iconType, String viewType, boolean active) {
        Button button = new Button(text);
        button.setUserData(text); // Store label for expand/collapse
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(18);
        button.setGraphic(icon);
        button.getStyleClass().add("nav-button");
        button.setPrefWidth(200);
        button.setPrefHeight(45);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphicTextGap(10);

        // Set button action
        button.setOnAction(e -> {
            if (onNavigate != null) {
                logger.debug("Navigation button clicked: {}", viewType);
                onNavigate.accept(viewType);
            }
        });

        // Apply initial state
        updateButtonState(button, active);

        return button;
    }

    /**
     * Updates the visual state of a navigation button.
     */
    private void updateButtonState(Button button, boolean active) {
        if (active) {
            button.getStyleClass().removeAll("nav-button");
            button.getStyleClass().add("nav-button-active");
        } else {
            button.getStyleClass().removeAll("nav-button-active");
            button.getStyleClass().add("nav-button");
        }
    }

    /**
     * Sets the active navigation button by view type.
     */
    public void setActiveButton(String viewType) {
        logger.debug("Setting active button: {}", viewType);

        // Deactivate all buttons
        for (Button btn : navigationButtons.values()) {
            updateButtonState(btn, false);
        }

        // Activate the selected button
        Button activeButton = navigationButtons.get(viewType);
        if (activeButton != null) {
            updateButtonState(activeButton, true);
        }
    }

    /**
     * Toggles the sidebar between collapsed and expanded states.
     */
    public void toggle() {
        collapsed = !collapsed;

        if (collapsed) {
            collapse();
        } else {
            expand();
        }

        logger.debug("Sidebar toggled: collapsed={}", collapsed);
    }

    /**
     * Collapses the sidebar to show only icons.
     */
    private void collapse() {
        sidebar.setPrefWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_COLLAPSED);
        sidebar.setPadding(new Insets(20, 5, 20, 5));

        // Hide resize handle when collapsed
        if (resizeHandle != null) {
            resizeHandle.setVisible(false);
            resizeHandle.setManaged(false);
        }

        // Hide text elements
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);

        // Hide search bar
        searchBar.setVisible(false);
        searchBar.setManaged(false);

        // Update buttons to show only icons
        for (Button btn : navigationButtons.values()) {
            btn.setText("");
            btn.setAlignment(Pos.CENTER);
            btn.setPrefWidth(50);
        }
    }

    /**
     * Expands the sidebar to show full content.
     */
    private void expand() {
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setPadding(new Insets(20));

        // Show resize handle when expanded
        if (resizeHandle != null) {
            resizeHandle.setVisible(true);
            resizeHandle.setManaged(true);
        }

        // Show text elements
        titleLabel.setVisible(true);
        titleLabel.setManaged(true);

        // Show search bar
        searchBar.setVisible(true);
        searchBar.setManaged(true);

        // Restore full button text and alignment
        for (Button btn : navigationButtons.values()) {
            if (btn.getUserData() instanceof String) {
                btn.setText((String) btn.getUserData());
            }
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setPrefWidth(200);
        }
    }

    /**
     * Updates button widths (used during resize).
     */
    public void updateButtonWidths(double sidebarWidth) {
        if (!collapsed) {
            double buttonWidth = sidebarWidth - 40; // Account for padding
            for (Button btn : navigationButtons.values()) {
                btn.setPrefWidth(buttonWidth);
            }
        }
    }

    /**
     * Gets the sidebar VBox for adding to layout.
     */
    public VBox getSidebar() {
        return sidebar;
    }

    /**
     * Gets the navigation buttons map.
     */
    public Map<String, Button> getNavigationButtons() {
        return navigationButtons;
    }

    /**
     * Checks if sidebar is currently collapsed.
     */
    public boolean isCollapsed() {
        return collapsed;
    }
}
