package com.roam.layout;

import com.roam.util.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Floating navigation bar component.
 * A horizontal pill-shaped bar positioned at bottom center of the window.
 * Contains navigation buttons and search functionality.
 */
public class FloatingNavBar extends StackPane {

    private static final Logger logger = LoggerFactory.getLogger(FloatingNavBar.class);

    private static final double BAR_HEIGHT = 56;
    private static final double BUTTON_SIZE = 44;
    private static final double ICON_SIZE = 20;
    private static final double SEARCH_EXPANDED_WIDTH = 200;

    private final HBox navBar;
    private final Map<String, Button> navigationButtons;
    private final Button searchBtn;
    private final TextField searchField;
    private final HBox searchContainer;

    private boolean searchExpanded = false;

    // Callbacks
    private final Consumer<String> onNavigate;
    private final Consumer<String> onSearch;

    public FloatingNavBar(Consumer<String> onNavigate, Consumer<String> onSearch) {
        this.onNavigate = onNavigate;
        this.onSearch = onSearch;
        this.navigationButtons = new HashMap<>();

        // Main container - don't expand to fill parent
        setAlignment(Pos.BOTTOM_CENTER);
        setPickOnBounds(false); // Allow clicks to pass through to content behind
        setMouseTransparent(false);

        // Make StackPane not fill parent - only wrap content
        setMaxHeight(BAR_HEIGHT + 40); // Bar height + padding
        setMaxWidth(USE_PREF_SIZE);

        // Navigation bar container
        navBar = new HBox(8);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(8, 16, 8, 16));
        navBar.getStyleClass().add("floating-nav-bar");
        navBar.setMaxHeight(BAR_HEIGHT);
        navBar.setMinHeight(BAR_HEIGHT);

        // Create navigation buttons
        Button operationsBtn = createNavButton("Operations", Feather.ACTIVITY, "operations");
        Button tasksBtn = createNavButton("Tasks", Feather.CHECK_SQUARE, "tasks");
        Button calendarBtn = createNavButton("Calendar", Feather.CALENDAR, "calendar");
        Button journalBtn = createNavButton("Journal", Feather.BOOK, "journal");
        Button wikiBtn = createNavButton("Wiki", Feather.BOOK_OPEN, "wiki");
        Button statisticsBtn = createNavButton("Statistics", Feather.BAR_CHART_2, "statistics");

        // Store references
        navigationButtons.put("operations", operationsBtn);
        navigationButtons.put("tasks", tasksBtn);
        navigationButtons.put("calendar", calendarBtn);
        navigationButtons.put("journal", journalBtn);
        navigationButtons.put("wiki", wikiBtn);
        navigationButtons.put("statistics", statisticsBtn);

        // Create search components
        searchBtn = createNavButton("Search", Feather.SEARCH, "search");
        searchBtn.setOnAction(e -> toggleSearch());

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("floating-search-field");
        searchField.setPrefWidth(0);
        searchField.setMaxWidth(0);
        searchField.setVisible(false);
        searchField.setOnAction(e -> {
            if (onSearch != null && !searchField.getText().isEmpty()) {
                onSearch.accept(searchField.getText());
            }
        });
        // Close search on focus lost
        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && searchExpanded) {
                collapseSearch();
            }
        });

        searchContainer = new HBox(4);
        searchContainer.setAlignment(Pos.CENTER);
        searchContainer.getChildren().addAll(searchField, searchBtn);

        // Divider
        StackPane divider = new StackPane();
        divider.getStyleClass().add("floating-nav-divider");
        divider.setPrefWidth(1);
        divider.setPrefHeight(24);

        // Add all to nav bar
        navBar.getChildren().addAll(
                operationsBtn,
                tasksBtn,
                calendarBtn,
                journalBtn,
                wikiBtn,
                statisticsBtn,
                divider,
                searchContainer);

        getChildren().add(navBar);

        // Set initial active state
        setActiveButton("wiki");

        // Apply theme
        applyTheme();

        logger.debug("FloatingNavBar initialized");
    }

    private Button createNavButton(String tooltip, Feather icon, String viewType) {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize((int) ICON_SIZE);
        fontIcon.getStyleClass().add("floating-nav-icon");
        button.setGraphic(fontIcon);
        button.getStyleClass().add("floating-nav-button");
        button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);

        // Tooltip
        Tooltip tip = new Tooltip(tooltip);
        tip.setShowDelay(Duration.millis(300));
        button.setTooltip(tip);

        // Action
        if (!viewType.equals("search")) {
            button.setOnAction(e -> {
                if (onNavigate != null) {
                    logger.debug("Navigation button clicked: {}", viewType);
                    onNavigate.accept(viewType);
                    setActiveButton(viewType);
                    animateButtonClick(button);
                }
            });
        }

        return button;
    }

    private void animateButtonClick(Button button) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private void toggleSearch() {
        if (searchExpanded) {
            collapseSearch();
        } else {
            expandSearch();
        }
    }

    private void expandSearch() {
        searchExpanded = true;
        searchField.setVisible(true);

        // Animate width
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(searchField.prefWidthProperty(), 0),
                        new javafx.animation.KeyValue(searchField.maxWidthProperty(), 0)),
                new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(searchField.prefWidthProperty(), SEARCH_EXPANDED_WIDTH),
                        new javafx.animation.KeyValue(searchField.maxWidthProperty(), SEARCH_EXPANDED_WIDTH)));
        timeline.play();
        timeline.setOnFinished(e -> searchField.requestFocus());

        // Update search button icon
        FontIcon icon = (FontIcon) searchBtn.getGraphic();
        icon.setIconCode(Feather.X);
    }

    private void collapseSearch() {
        searchExpanded = false;

        // Animate width
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(searchField.prefWidthProperty(), SEARCH_EXPANDED_WIDTH),
                        new javafx.animation.KeyValue(searchField.maxWidthProperty(), SEARCH_EXPANDED_WIDTH)),
                new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(searchField.prefWidthProperty(), 0),
                        new javafx.animation.KeyValue(searchField.maxWidthProperty(), 0)));
        timeline.setOnFinished(e -> {
            searchField.setVisible(false);
            searchField.clear();
        });
        timeline.play();

        // Update search button icon
        FontIcon icon = (FontIcon) searchBtn.getGraphic();
        icon.setIconCode(Feather.SEARCH);
    }

    /**
     * Sets the active navigation button by view type.
     */
    public void setActiveButton(String viewType) {
        logger.debug("Setting active button: {}", viewType);

        // Deactivate all buttons
        for (Button btn : navigationButtons.values()) {
            btn.getStyleClass().remove("selected");
        }

        // Activate the selected button
        Button activeButton = navigationButtons.get(viewType);
        if (activeButton != null && !activeButton.getStyleClass().contains("selected")) {
            activeButton.getStyleClass().add("selected");
        }
    }

    /**
     * Gets the navigation buttons map.
     */
    public Map<String, Button> getNavigationButtons() {
        return navigationButtons;
    }

    /**
     * Apply current theme styling
     */
    public void applyTheme() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();

        if (isDark) {
            navBar.setStyle("-fx-background-color: #2d2d2d; " +
                    "-fx-background-radius: 16; " +
                    "-fx-border-color: rgba(255, 255, 255, 0.06); " +
                    "-fx-border-radius: 16; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 16, 0, 0, 4);");
        } else {
            navBar.setStyle("-fx-background-color: #ffffff; " +
                    "-fx-background-radius: 16; " +
                    "-fx-border-color: rgba(0, 0, 0, 0.06); " +
                    "-fx-border-radius: 16; " +
                    "-fx-border-width: 1; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 4);");
        }
    }

    /**
     * Refresh theme when it changes
     */
    public void refreshTheme() {
        applyTheme();
    }
}
