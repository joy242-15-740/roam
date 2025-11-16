package com.roam.view.components;

import com.roam.model.Priority;
import com.roam.model.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class TaskCard extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    private final Task task;
    private final Consumer<Task> onEdit;

    public TaskCard(Task task, Consumer<Task> onEdit) {
        this.task = task;
        this.onEdit = onEdit;

        setSpacing(8);
        setPadding(new Insets(15));
        setMinHeight(100);
        setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-left-width: 4; " +
                        "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + ";");

        // Hover effect
        setOnMouseEntered(e -> {
            setStyle(
                    "-fx-background-color: #FFFFFF; " +
                            "-fx-border-color: #E0E0E0; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-left-width: 4; " +
                            "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + "; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                            "-fx-scale-x: 1.02; " +
                            "-fx-scale-y: 1.02;");
        });

        setOnMouseExited(e -> {
            setStyle(
                    "-fx-background-color: #FFFFFF; " +
                            "-fx-border-color: #E0E0E0; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-left-width: 4; " +
                            "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + ";");
        });

        // Title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 14));
        titleLabel.setStyle("-fx-text-fill: #000000;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Description (if exists)
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.setFont(Font.font("Poppins Regular", 12));
            descLabel.setStyle("-fx-text-fill: #616161;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);

            // Limit to 3 lines
            String desc = task.getDescription();
            if (desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }
            descLabel.setText(desc);

            getChildren().addAll(titleLabel, descLabel);
        } else {
            getChildren().add(titleLabel);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(spacer);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Due date
        if (task.getDueDate() != null) {
            Label dueDateLabel = new Label("📅 " + DATE_FORMATTER.format(task.getDueDate()));
            dueDateLabel.setFont(Font.font("Poppins Regular", 11));
            dueDateLabel.setStyle("-fx-text-fill: #9E9E9E;");
            footer.getChildren().add(dueDateLabel);
        }

        getChildren().add(footer);
    }

    private String getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> "#C62828";
            case MEDIUM -> "#F9A825";
            case LOW -> "#616161";
        };
    }

    public Task getTask() {
        return task;
    }
}
