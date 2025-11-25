package com.roam;

import com.roam.layout.NavigationManager;
import com.roam.layout.SidebarComponent;
import com.roam.layout.SidebarResizeHandler;
import com.roam.layout.ViewFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application layout coordinating sidebar navigation and content display.
 * Delegates to specialized components for sidebar, navigation, and resizing.
 */
public class MainLayout extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);

    // Content area
    private StackPane contentArea;

    // Components
    private ViewFactory viewFactory;
    private NavigationManager navigationManager;
    private SidebarComponent sidebarComponent;
    private SidebarResizeHandler resizeHandler;

    public MainLayout() {
        logger.debug("Initializing MainLayout");
        initializeLayout();
    }

    private void initializeLayout() {
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30));
        contentArea.getStyleClass().add("content-area");

        // Create view factory
        viewFactory = new ViewFactory();

        // Create resize handler with temporary sidebar (will be updated after sidebar
        // creation)
        VBox tempSidebar = new VBox();
        resizeHandler = new SidebarResizeHandler(tempSidebar, this::updateSidebarButtonWidths);

        // Create sidebar component
        sidebarComponent = new SidebarComponent(
                this::handleNavigation,
                this::handleSearch,
                null, // Toggle callback not needed as SidebarComponent handles it internally
                resizeHandler.getResizeHandle());

        // Update resize handler with actual sidebar
        resizeHandler = new SidebarResizeHandler(
                sidebarComponent.getSidebar(),
                this::updateSidebarButtonWidths);

        // Create navigation manager
        navigationManager = new NavigationManager(
                contentArea,
                viewFactory,
                sidebarComponent.getNavigationButtons(),
                this::updateButtonStates);

        // Create sidebar container with resize handle
        HBox sidebarContainer = new HBox();
        sidebarContainer.getChildren().addAll(
                sidebarComponent.getSidebar(),
                resizeHandler.getResizeHandle());

        // Set layout regions
        setLeft(sidebarContainer);
        setCenter(contentArea);

        // Show default view (wiki)
        navigationManager.navigateToView("wiki");

        logger.info("MainLayout initialized successfully");
    }

    /**
     * Callback to handle navigation button clicks from SidebarComponent.
     */
    private void handleNavigation(String viewType) {
        logger.debug("Handle navigation request: {}", viewType);
        navigationManager.navigateToView(viewType);
    }

    /**
     * Callback to handle search requests from SidebarComponent.
     */
    private void handleSearch(String query) {
        logger.debug("Handle search request: {}", query);
        navigationManager.performSearch(query);
    }

    /**
     * Callback to update button states when navigation occurs.
     */
    private void updateButtonStates(String activeViewType) {
        logger.debug("Updating button states: active={}", activeViewType);
        sidebarComponent.setActiveButton(activeViewType);
    }

    /**
     * Callback to update sidebar button widths when sidebar is resized.
     */
    private void updateSidebarButtonWidths() {
        double sidebarWidth = sidebarComponent.getSidebar().getWidth();
        sidebarComponent.updateButtonWidths(sidebarWidth);
    }
}
