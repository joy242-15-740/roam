package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.view.components.WikiNoteEditor;
import com.roam.view.components.WikiSidebar;
import com.roam.view.components.WikiToolbar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class WikiView extends BorderPane {

    private final WikiController controller;
    private final WikiToolbar toolbar;
    private final WikiSidebar sidebar;
    private final WikiNoteEditor noteEditor;
    private final Font poppinsRegular;
    private final Font poppinsBold;

    public WikiView(WikiController controller, Font poppinsRegular, Font poppinsBold) {
        this.controller = controller;
        this.poppinsRegular = poppinsRegular;
        this.poppinsBold = poppinsBold;

        this.toolbar = new WikiToolbar(controller, poppinsRegular, poppinsBold);
        this.sidebar = new WikiSidebar(controller, poppinsRegular, poppinsBold);
        this.noteEditor = new WikiNoteEditor(controller, poppinsRegular, poppinsBold);

        initializeLayout();
    }

    private void initializeLayout() {
        // Set components in BorderPane regions
        setTop(toolbar);
        setLeft(sidebar);
        setCenter(noteEditor);

        // Set background
        setStyle("-fx-background-color: -roam-bg-primary;");

        // Load initial data
        controller.loadAllNotes();
        sidebar.refreshAll();
    }

    public void refresh() {
        sidebar.refreshAll();
        controller.loadAllNotes();
    }
}
