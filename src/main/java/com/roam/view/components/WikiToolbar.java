package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.WikiTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.util.List;

public class WikiToolbar extends HBox {

    private final WikiController controller;
    private final Font poppinsRegular;
    private final Font poppinsBold;

    private Button newNoteBtn;
    private MenuButton templatesMenu;

    public WikiToolbar(WikiController controller, Font poppinsRegular, Font poppinsBold) {
        this.controller = controller;
        this.poppinsRegular = poppinsRegular;
        this.poppinsBold = poppinsBold;

        configureToolbar();
        createToolbarElements();
        setupEventHandlers();
    }

    private void configureToolbar() {
        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
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
        btn.setFont(Font.font(poppinsBold.getFamily(), 14));
        btn.setPrefWidth(130);
        btn.setPrefHeight(40);
        btn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: -roam-blue-dark; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));

        return btn;
    }

    private MenuButton createTemplatesMenu() {
        MenuButton menu = new MenuButton("Templates ▼");
        menu.setFont(Font.font(poppinsRegular.getFamily(), 14));
        menu.setPrefWidth(120);
        menu.setPrefHeight(40);
        menu.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

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
            item.setStyle("-fx-font-family: '" + poppinsRegular.getFamily() + "'; -fx-font-size: 13px;");
            item.setOnAction(e -> {
                controller.createNoteFromTemplate(template);
            });
            menu.getItems().add(item);
        }

        if (!allTemplates.isEmpty()) {
            menu.getItems().add(new SeparatorMenuItem());
        }

        MenuItem manageItem = new MenuItem("Manage Templates...");
        manageItem.setStyle("-fx-font-family: '" + poppinsRegular.getFamily() + "'; -fx-font-size: 13px;");
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
