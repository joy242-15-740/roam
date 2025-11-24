package com.roam.view;

import com.roam.controller.TasksController;
import com.roam.view.components.BatchOperationsBar;
import com.roam.view.components.GlobalTasksKanban;
import com.roam.view.components.TasksFilterPanel;
import com.roam.view.components.TasksListView;
import com.roam.view.components.TasksStatsBar;
import com.roam.view.components.TasksTimelineView;
import com.roam.view.components.TasksEisenhowerView;
import com.roam.view.components.TasksToolbar;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TasksView extends BorderPane {

    private final TasksController controller;
    private final TasksToolbar toolbar;
    private final TasksFilterPanel filterPanel;
    private final TasksStatsBar statsBar;
    private final GlobalTasksKanban kanbanView;
    private final TasksListView listView;
    private final TasksTimelineView timelineView;
    private final TasksEisenhowerView matrixView;
    private final BatchOperationsBar batchBar;

    public TasksView(TasksController controller) {
        this.controller = controller;

        setStyle("-fx-background-color: -roam-bg-primary;");

        // Create toolbar
        toolbar = new TasksToolbar(controller);
        toolbar.setOnViewChanged(this::switchView);

        // Create filter panel
        filterPanel = new TasksFilterPanel(controller);

        // Create stats bar
        statsBar = new TasksStatsBar(controller);

        // Create kanban view
        kanbanView = new GlobalTasksKanban(controller);

        // Create list view
        listView = new TasksListView(controller);

        // Create timeline view
        timelineView = new TasksTimelineView(controller);

        // Create Eisenhower matrix view
        matrixView = new TasksEisenhowerView(controller);

        // Create batch operations bar
        batchBar = new BatchOperationsBar(controller);

        // Combine toolbar, filter panel, and stats bar
        VBox topContainer = new VBox(toolbar, filterPanel, statsBar);

        setTop(topContainer);

        // Use StackPane for center to allow batch bar overlay
        StackPane centerStack = new StackPane();
        centerStack.getChildren().add(kanbanView);
        centerStack.getChildren().add(batchBar);
        StackPane.setAlignment(batchBar, Pos.BOTTOM_CENTER);

        setCenter(centerStack);

        // Set up data change listener
        controller.setOnDataChanged(this::refreshView);

        // Initial load
        refreshView();
    }

    private void switchView(String viewName) {
        StackPane centerStack = new StackPane();

        switch (viewName) {
            case "kanban" -> {
                centerStack.getChildren().addAll(kanbanView, batchBar);
            }
            case "list" -> {
                centerStack.getChildren().addAll(listView, batchBar);
            }
            case "timeline" -> {
                centerStack.getChildren().add(timelineView);
            }
            case "matrix" -> {
                centerStack.getChildren().add(matrixView);
            }
        }

        StackPane.setAlignment(batchBar, Pos.BOTTOM_CENTER);
        setCenter(centerStack);
        refreshView();
    }

    private void refreshView() {
        String selectedView = toolbar.getSelectedView();

        switch (selectedView) {
            case "kanban" -> kanbanView.loadTasks(controller.loadTasks());
            case "list" -> listView.loadTasks(controller.loadTasks());
            case "timeline" -> timelineView.loadTasks(controller.loadTasks());
            case "matrix" -> matrixView.loadTasks(controller.loadTasks());
        }

        statsBar.updateStats();
        filterPanel.refresh();
    }
}
