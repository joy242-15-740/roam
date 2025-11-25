package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.view.components.WikiNoteEditor;
import com.roam.view.components.WikiSidebar;
import com.roam.view.components.WikiToolbar;
import javafx.scene.layout.BorderPane;

public class WikiView extends BorderPane {

    private final WikiController controller;
    private final WikiToolbar toolbar;
    private final WikiSidebar sidebar;
    private final WikiNoteEditor noteEditor;

    public WikiView(WikiController controller) {
        this.controller = controller;

        this.toolbar = new WikiToolbar(controller);
        this.sidebar = new WikiSidebar(controller);
        this.noteEditor = new WikiNoteEditor(controller);

        initializeLayout();
    }

    private void initializeLayout() {
        // Set components in BorderPane regions
        setTop(toolbar);
        setLeft(sidebar);
        setCenter(noteEditor);

        // Set background
        getStyleClass().add("wiki-view");

        // Load initial data
        controller.loadAllNotes();
        sidebar.refreshAll();
    }

    public void refresh() {
        sidebar.refreshAll();
        controller.loadAllNotes();
    }
}
