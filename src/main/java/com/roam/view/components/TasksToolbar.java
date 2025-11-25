package com.roam.view.components;

import atlantafx.base.theme.Styles;
import com.roam.controller.TasksController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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
        // Remove manual border/bg, let theme handle or use simple border
        setStyle("-fx-border-color: -color-border-default; -fx-border-width: 0 0 1 0;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View toggle buttons
        viewToggleGroup = new ToggleGroup();
        ToggleButton kanbanBtn = createViewToggleButton("Kanban", Styles.LEFT_PILL);
        ToggleButton listBtn = createViewToggleButton("List", Styles.CENTER_PILL);
        ToggleButton timelineBtn = createViewToggleButton("Timeline", Styles.CENTER_PILL);
        ToggleButton matrixBtn = createViewToggleButton("Matrix", Styles.RIGHT_PILL);

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
        // No manual border needed if using PILL styles correctly, but might need
        // alignment
        viewToggle.setAlignment(Pos.CENTER);

        // New Task button
        Button newTaskBtn = new Button("+ New Task");
        newTaskBtn.setPrefWidth(130);
        newTaskBtn.setPrefHeight(40);
        newTaskBtn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        newTaskBtn.setOnAction(e -> {
            controller.createTask();
        });

        getChildren().addAll(spacer, viewToggle, newTaskBtn);
    }

    private ToggleButton createViewToggleButton(String text, String styleClass) {
        ToggleButton btn = new ToggleButton(text);
        btn.setPrefHeight(40);
        btn.setPrefWidth(90);
        btn.getStyleClass().add(styleClass);
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
