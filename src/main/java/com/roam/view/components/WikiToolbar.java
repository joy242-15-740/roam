package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.WikiTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.List;

public class WikiToolbar extends HBox {

    private final WikiController controller;

    private Button newNoteBtn;
    private MenuButton templatesMenu;

    public WikiToolbar(WikiController controller) {
        this.controller = controller;

        configureToolbar();
        createToolbarElements();
        setupEventHandlers();
    }

    private void configureToolbar() {
        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("wiki-toolbar");
        setStyle("-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");
    }

    private void createToolbarElements() {
        newNoteBtn = createNewNoteButton();
        templatesMenu = createTemplatesMenu();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(newNoteBtn, templatesMenu, spacer);
    }

    private Button createNewNoteButton() {
        Button btn = new Button("+ New Wiki");
        btn.getStyleClass().add("action-button");
        btn.setPrefWidth(130);
        btn.setPrefHeight(40);
        return btn;
    }

    private MenuButton createTemplatesMenu() {
        MenuButton menu = new MenuButton("Templates");
        menu.getStyleClass().add("button-secondary");
        menu.setPrefWidth(120);
        menu.setPrefHeight(40);

        // Populate on show
        menu.setOnShowing(e -> populateTemplatesMenu(menu));

        return menu;
    }

    private void populateTemplatesMenu(MenuButton menu) {
        menu.getItems().clear();

        // Load all templates
        List<WikiTemplate> allTemplates = controller.loadAllTemplates();
        for (WikiTemplate template : allTemplates) {
            MenuItem item = new MenuItem(template.getIcon() + " " + template.getName());
            item.setOnAction(e -> {
                controller.createNoteFromTemplate(template);
            });
            menu.getItems().add(item);
        }

        if (!allTemplates.isEmpty()) {
            menu.getItems().add(new SeparatorMenuItem());
        }

        MenuItem manageItem = new MenuItem("Manage Templates...");
        manageItem.setOnAction(e -> {
            TemplateManagerDialog dialog = new TemplateManagerDialog(controller);
            dialog.showAndWait();
        });
        menu.getItems().add(manageItem);
    }

    private void setupEventHandlers() {
        // New Wiki button
        newNoteBtn.setOnAction(e -> {
            controller.createNewNote();
        });
    }
}
