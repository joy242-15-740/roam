package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.view.components.WikiNoteEditor;
import com.roam.view.components.WikiSidebar;
import com.roam.view.components.WikiToolbar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class WikiView extends StackPane {

    private final WikiController controller;
    private final WikiToolbar toolbar;
    private final WikiSidebar sidebar;
    private final WikiNoteEditor noteEditor;
    private final Font poppinsRegular;
    private final Font poppinsBold;
    private final BorderPane contentPane;

    public WikiView(WikiController controller, Font poppinsRegular, Font poppinsBold) {
        this.controller = controller;
        this.poppinsRegular = poppinsRegular;
        this.poppinsBold = poppinsBold;
        this.contentPane = new BorderPane();
        getChildren().add(contentPane);

        this.toolbar = new WikiToolbar(controller, poppinsRegular, poppinsBold);
        this.sidebar = new WikiSidebar(controller, poppinsRegular, poppinsBold);
        this.noteEditor = new WikiNoteEditor(controller, poppinsRegular, poppinsBold);

        initializeLayout();

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

    private void initializeLayout() {
        // Set components in BorderPane regions
        contentPane.setTop(toolbar);
        contentPane.setLeft(sidebar);
        contentPane.setCenter(noteEditor);

        // Set background
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Load initial data
        controller.loadAllNotes();
        sidebar.refreshAll();
    }

    public void refresh() {
        sidebar.refreshAll();
        controller.loadAllNotes();
    }
}
