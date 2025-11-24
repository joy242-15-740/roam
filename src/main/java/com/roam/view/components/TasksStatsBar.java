package com.roam.view.components;

import com.roam.controller.TasksController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TasksStatsBar extends HBox {

    private final TasksController controller;

    private Label totalLabel;
    private Label todoLabel;
    private Label inProgressLabel;
    private Label doneLabel;
    private Label highPriorityLabel;
    private Label overdueLabel;

    public TasksStatsBar(TasksController controller) {
        this.controller = controller;

        setPrefHeight(50);
        setPadding(new Insets(10, 20, 10, 20));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(30);
        setStyle("-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        // Create stat items
        VBox totalStat = createStatItem("📋", "0", "Total", "-roam-text-primary");
        totalLabel = (Label) ((VBox) totalStat.getChildren().get(0)).getChildren().get(1);

        VBox todoStat = createStatItem("⭕", "0", "To Do", "-roam-blue");
        todoLabel = (Label) ((VBox) todoStat.getChildren().get(0)).getChildren().get(1);

        VBox inProgressStat = createStatItem("⏳", "0", "In Progress", "-roam-orange");
        inProgressLabel = (Label) ((VBox) inProgressStat.getChildren().get(0)).getChildren().get(1);

        VBox doneStat = createStatItem("✓", "0", "Done", "-roam-green");
        doneLabel = (Label) ((VBox) doneStat.getChildren().get(0)).getChildren().get(1);

        VBox highPriorityStat = createStatItem("🔥", "0", "High Priority", "-roam-red");
        highPriorityLabel = (Label) ((VBox) highPriorityStat.getChildren().get(0)).getChildren().get(1);

        VBox overdueStat = createStatItem("⚠️", "0", "Overdue", "-roam-red");
        overdueLabel = (Label) ((VBox) overdueStat.getChildren().get(0)).getChildren().get(1);

        getChildren().addAll(totalStat, todoStat, inProgressStat, doneStat, highPriorityStat, overdueStat);

        // Initial update
        updateStats();
    }

    private VBox createStatItem(String icon, String count, String label, String color) {
        VBox container = new VBox(2);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(5, 10, 5, 10));
        container.setStyle("-fx-background-radius: 6; -fx-cursor: hand;");

        VBox content = new VBox(0);
        content.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(16));

        Label countLabel = new Label(count);
        countLabel.setFont(Font.font("Poppins Bold", 20));
        countLabel.setStyle("-fx-text-fill: " + color + ";");

        content.getChildren().addAll(iconLabel, countLabel);

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("Poppins Regular", 12));
        textLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        container.getChildren().addAll(content, textLabel);

        // Hover effect
        container.setOnMouseEntered(
                e -> container
                        .setStyle("-fx-background-color: -roam-gray-bg; -fx-background-radius: 6; -fx-cursor: hand;"));
        container.setOnMouseExited(e -> container.setStyle("-fx-background-radius: 6; -fx-cursor: hand;"));

        return container;
    }

    public void updateStats() {
        TasksController.TaskStats stats = controller.calculateStats();

        totalLabel.setText(String.valueOf(stats.getTotal()));
        todoLabel.setText(String.valueOf(stats.getTodoCount()));
        inProgressLabel.setText(String.valueOf(stats.getInProgressCount()));
        doneLabel.setText(String.valueOf(stats.getDoneCount()));
        highPriorityLabel.setText(String.valueOf(stats.getHighPriorityCount()));
        overdueLabel.setText(String.valueOf(stats.getOverdueCount()));
    }
}
