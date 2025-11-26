package com.roam.view;

import com.roam.layout.CustomTitleBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Lock screen layout with custom title bar.
 * Wraps the LockScreen component with the application's custom title bar.
 */
public class LockScreenLayout extends BorderPane {

    private final CustomTitleBar titleBar;
    private final LockScreen lockScreen;

    public LockScreenLayout(Stage stage, Runnable onUnlock) {
        // Add style class for rounded corners
        getStyleClass().add("main-layout");

        // Create custom title bar
        titleBar = new CustomTitleBar(stage);

        // Create lock screen content
        lockScreen = new LockScreen(onUnlock);

        // Add rounded corner style to lock screen
        lockScreen.setStyle("-fx-background-radius: 0 0 10 10;");

        // Set layout
        setTop(titleBar);
        setCenter(lockScreen);
    }

    /**
     * Get the title bar for theme updates
     */
    public CustomTitleBar getTitleBar() {
        return titleBar;
    }

    /**
     * Refresh theme on title bar
     */
    public void refreshTheme() {
        titleBar.refreshTheme();
    }
}
