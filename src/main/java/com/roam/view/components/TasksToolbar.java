package com.roam.view.components;

import com.roam.controller.TasksController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.util.function.Consumer;

public class TasksToolbar extends HBox {

    private final TasksController controller;
    private final ToggleGroup viewToggleGroup;
    private Consumer<String> onViewChanged;

    public TasksToolbar(TasksController controller) {
        this.controller = controller;

        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(15);
        setStyle("-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View toggle buttons
        viewToggleGroup = new ToggleGroup();
        ToggleButton kanbanBtn = createViewToggleButton("Kanban", true, false);
        ToggleButton listBtn = createViewToggleButton("List", false, false);
        ToggleButton timelineBtn = createViewToggleButton("Timeline", false, false);
        ToggleButton matrixBtn = createViewToggleButton("Matrix", false, true);

        kanbanBtn.setToggleGroup(viewToggleGroup);
        listBtn.setToggleGroup(viewToggleGroup);
        timelineBtn.setToggleGroup(viewToggleGroup);
        matrixBtn.setToggleGroup(viewToggleGroup);
        kanbanBtn.setSelected(true);

        kanbanBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("kanban");
        });
        listBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("list");
        });
        timelineBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("timeline");
        });
        matrixBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("matrix");
        });

        HBox viewToggle = new HBox(0, kanbanBtn, listBtn, timelineBtn, matrixBtn);
        viewToggle.setStyle(
                "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

        // New Task button
        Button newTaskBtn = new Button("+ New Task");
        newTaskBtn.setFont(Font.font("Poppins Medium", 14));
        newTaskBtn.setPrefWidth(130);
        newTaskBtn.setPrefHeight(40);
        newTaskBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        newTaskBtn.setOnMouseEntered(e -> newTaskBtn.setStyle(
                "-fx-background-color: -roam-blue-dark; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));
        newTaskBtn.setOnMouseExited(e -> newTaskBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"));
        newTaskBtn.setOnAction(e -> {
            controller.createTask();
        });

        getChildren().addAll(spacer, viewToggle, newTaskBtn);
    }

    private ToggleButton createViewToggleButton(String text, boolean first, boolean last) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Font.font("Poppins Regular", 13));
        btn.setPrefHeight(40);
        btn.setPrefWidth(90);

        final String baseStyle;
        final String selectedStyle;

        if (first) {
            baseStyle = "-fx-background-color: transparent; " +
                    "-fx-text-fill: -roam-text-secondary; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 8 0 0 8;";
            selectedStyle = "-fx-background-color: -roam-blue; " +
                    "-fx-text-fill: white; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 8 0 0 8;";
        } else if (last) {
            baseStyle = "-fx-background-color: transparent; " +
                    "-fx-text-fill: -roam-text-secondary; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 0 8 8 0;";
            selectedStyle = "-fx-background-color: -roam-blue; " +
                    "-fx-text-fill: white; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 0 8 8 0;";
        } else {
            baseStyle = "-fx-background-color: transparent; " +
                    "-fx-text-fill: -roam-text-secondary; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 0;";
            selectedStyle = "-fx-background-color: -roam-blue; " +
                    "-fx-text-fill: white; " +
                    "-fx-cursor: hand; " +
                    "-fx-border-width: 0; " +
                    "-fx-background-radius: 0;";
        }

        btn.setStyle(baseStyle);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle(selectedStyle);
            } else {
                btn.setStyle(baseStyle);
            }
        });

        return btn;
    }

    public void setOnViewChanged(Consumer<String> callback) {
        this.onViewChanged = callback;
    }

    public String getSelectedView() {
        if (viewToggleGroup.getSelectedToggle() != null) {
            String text = ((ToggleButton) viewToggleGroup.getSelectedToggle()).getText();
            return text.toLowerCase();
        }
        return "kanban";
    }
}
