package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class GlobalTasksKanban extends HBox {

    private final TasksController controller;

    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;

    private final VBox todoTasksContainer;
    private final VBox inProgressTasksContainer;
    private final VBox doneTasksContainer;

    private final Label todoCountLabel;
    private final Label inProgressCountLabel;
    private final Label doneCountLabel;

    public GlobalTasksKanban(TasksController controller) {
        this.controller = controller;

        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: -roam-bg-primary;");
        setFillHeight(true);

        // Create task containers
        todoTasksContainer = new VBox(10);
        inProgressTasksContainer = new VBox(10);
        doneTasksContainer = new VBox(10);

        // Create count labels
        todoCountLabel = new Label("0");
        inProgressCountLabel = new Label("0");
        doneCountLabel = new Label("0");

        // Create columns
        todoColumn = createColumn("To Do", "-roam-blue-light", "-roam-blue", TaskStatus.TODO,
                todoTasksContainer, todoCountLabel);
        inProgressColumn = createColumn("In Progress", "#FFF3E0", "#F57C00", TaskStatus.IN_PROGRESS,
                inProgressTasksContainer, inProgressCountLabel);
        doneColumn = createColumn("Done", "#E8F5E9", "#388E3C", TaskStatus.DONE,
                doneTasksContainer, doneCountLabel);

        // Make columns equal width
        HBox.setHgrow(todoColumn, Priority.ALWAYS);
        HBox.setHgrow(inProgressColumn, Priority.ALWAYS);
        HBox.setHgrow(doneColumn, Priority.ALWAYS);

        getChildren().addAll(todoColumn, inProgressColumn, doneColumn);
    }

    private VBox createColumn(String title, String headerBg, String textColor,
            TaskStatus status, VBox tasksContainer, Label countLabel) {
        VBox column = new VBox();
        column.setMinWidth(280);
        column.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

        // Header
        HBox header = new HBox(10);
        header.setPrefHeight(50);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + headerBg + "; " +
                "-fx-background-radius: 8 8 0 0;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins Bold", 16));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Count badge
        countLabel.setFont(Font.font("Poppins Regular", 12));
        countLabel.setStyle(
                "-fx-background-color: " + headerBg + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-border-color: " + textColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 2 8 2 8;");

        header.getChildren().addAll(titleLabel, countLabel);

        // Add task button
        Button addBtn = new Button("+ Add Task");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Poppins Regular", 13));
        addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnAction(e -> createTaskWithStatus(status));
        VBox.setMargin(addBtn, new Insets(10));

        // Scrollable tasks container
        tasksContainer.setPadding(new Insets(10));
        tasksContainer.setSpacing(10);

        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Enable drag and drop
        setupDragAndDrop(tasksContainer, status);

        column.getChildren().addAll(header, addBtn, scrollPane);
        return column;
    }

    private void createTaskWithStatus(TaskStatus status) {
        controller.createTask(status);
    }

    private void setupDragAndDrop(VBox container, TaskStatus targetStatus) {
        container.setOnDragOver(event -> {
            if (event.getGestureSource() != container && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
            event.consume();
        });

        container.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                try {
                    Long taskId = Long.parseLong(db.getString());
                    Task task = findTaskById(taskId);
                    if (task != null) {
                        controller.updateTaskStatus(taskId, targetStatus);
                        success = true;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private Task findTaskById(Long taskId) {
        for (javafx.scene.Node node : todoTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : inProgressTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : doneTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        return null;
    }

    public void loadTasks(List<Task> tasks) {
        // Clear all containers
        todoTasksContainer.getChildren().clear();
        inProgressTasksContainer.getChildren().clear();
        doneTasksContainer.getChildren().clear();

        int todoCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;

        // Sort tasks by status
        for (Task task : tasks) {
            GlobalTaskCard card = new GlobalTaskCard(task, controller);

            // Enable drag
            card.setOnDragDetected(event -> {
                javafx.scene.input.Dragboard db = card.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(task.getId().toString());
                db.setContent(content);
                card.setOpacity(0.6);
                event.consume();
            });

            card.setOnDragDone(event -> {
                card.setOpacity(1.0);
                event.consume();
            });

            switch (task.getStatus()) {
                case TODO -> {
                    todoTasksContainer.getChildren().add(card);
                    todoCount++;
                }
                case IN_PROGRESS -> {
                    inProgressTasksContainer.getChildren().add(card);
                    inProgressCount++;
                }
                case DONE -> {
                    doneTasksContainer.getChildren().add(card);
                    doneCount++;
                }
            }
        }

        // Update counts
        todoCountLabel.setText(String.valueOf(todoCount));
        inProgressCountLabel.setText(String.valueOf(inProgressCount));
        doneCountLabel.setText(String.valueOf(doneCount));
    }

    // Enhanced task card with operation badge and selection
    private static class GlobalTaskCard extends VBox {
        private final Task task;
        private final Button editBtn;
        private final Button completeBtn;

        public GlobalTaskCard(Task task, TasksController controller) {
            this.task = task;
            this.editBtn = new Button();
            this.completeBtn = new Button();

            setSpacing(8);
            setPadding(new Insets(15));
            setMinHeight(120);
            setStyle(
                    "-fx-background-color: -roam-bg-primary; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-left-width: 4; " +
                            "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + ";");

            // Hover effect
            setOnMouseEntered(e -> {
                setStyle(
                        "-fx-background-color: -roam-bg-primary; " +
                                "-fx-border-color: -roam-border; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-left-width: 4; " +
                                "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + "; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
                editBtn.setVisible(true);
                if (task.getStatus() != TaskStatus.DONE) {
                    completeBtn.setVisible(true);
                }
            });

            setOnMouseExited(e -> {
                setStyle(
                        "-fx-background-color: -roam-bg-primary; " +
                                "-fx-border-color: -roam-border; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 8; " +
                                "-fx-background-radius: 8; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-left-width: 4; " +
                                "-fx-border-left-color: " + getPriorityColor(task.getPriority()) + ";");
                editBtn.setVisible(false);
                completeBtn.setVisible(false);
            });

            setOnMouseClicked(e -> {
                if (e.getTarget() != editBtn && e.getTarget() != completeBtn
                        && !editBtn.contains(editBtn.sceneToLocal(e.getSceneX(), e.getSceneY()))
                        && !completeBtn.contains(completeBtn.sceneToLocal(e.getSceneX(), e.getSceneY()))) {
                    controller.editTask(task);
                }
            });

            // Top row: Operation badge + Edit Button + Complete Button
            HBox topRow = new HBox(5);
            topRow.setAlignment(Pos.CENTER_LEFT);

            // Operation badge
            if (task.getOperationId() != null) {
                controller.getOperationById(task.getOperationId()).ifPresent(operation -> {
                    Label opBadge = new Label(operation.getName());
                    opBadge.setFont(Font.font("Poppins Regular", 11));
                    opBadge.setStyle(
                            "-fx-background-color: -roam-blue-light; " +
                                    "-fx-text-fill: -roam-blue; " +
                                    "-fx-padding: 3 8 3 8; " +
                                    "-fx-background-radius: 10;");
                    topRow.getChildren().add(opBadge);
                });
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            topRow.getChildren().add(spacer);

            // Edit button
            FontIcon editIcon = new FontIcon(Feather.EDIT_2);
            editIcon.setIconSize(14);
            editBtn.setGraphic(editIcon);
            editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
            editBtn.setVisible(false);
            editBtn.setOnAction(e -> {
                e.consume();
                controller.editTask(task);
            });
            topRow.getChildren().add(editBtn);

            // Complete button
            FontIcon completeIcon = new FontIcon(Feather.CHECK_CIRCLE);
            completeIcon.setIconSize(14);
            completeIcon.setIconColor(javafx.scene.paint.Color.web("#388E3C"));
            completeBtn.setGraphic(completeIcon);
            completeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
            completeBtn.setVisible(false);
            completeBtn.setOnAction(e -> {
                e.consume();
                controller.updateTaskStatus(task, TaskStatus.DONE);
            });

            if (task.getStatus() != TaskStatus.DONE) {
                topRow.getChildren().add(completeBtn);
            }

            // Title
            Label titleLabel = new Label(task.getTitle());
            titleLabel.setFont(Font.font("Poppins Medium", 14));
            titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            getChildren().addAll(topRow, titleLabel);

            // Description (if exists)
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                Label descLabel = new Label(task.getDescription());
                descLabel.setFont(Font.font("Poppins Regular", 12));
                descLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(Double.MAX_VALUE);

                String desc = task.getDescription();
                if (desc.length() > 80) {
                    desc = desc.substring(0, 80) + "...";
                }
                descLabel.setText(desc);

                getChildren().add(descLabel);
            }

            // Spacer
            Region cardSpacer = new Region();
            VBox.setVgrow(cardSpacer, Priority.ALWAYS);
            getChildren().add(cardSpacer);

            // Footer
            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_LEFT);

            // Due date
            if (task.getDueDate() != null) {
                Label dueDateLabel = new Label("📅 " + formatDueDate(task.getDueDate()));
                dueDateLabel.setFont(Font.font("Poppins Regular", 11));
                dueDateLabel.setStyle("-fx-text-fill: -roam-text-hint;");
                footer.getChildren().add(dueDateLabel);
            }

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);
            footer.getChildren().add(footerSpacer);

            // Assignee
            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                String initials = getInitials(task.getAssignee());
                Label assigneeLabel = new Label("👤 " + initials);
                assigneeLabel.setFont(Font.font("Poppins Regular", 11));
                assigneeLabel.setStyle("-fx-text-fill: -roam-text-hint;");
                footer.getChildren().add(assigneeLabel);
            }

            getChildren().add(footer);
        }

        public Task getTask() {
            return task;
        }

        private String getPriorityColor(com.roam.model.Priority priority) {
            return switch (priority) {
                case HIGH -> "#C62828";
                case MEDIUM -> "#F9A825";
                case LOW -> "#616161";
            };
        }

        private String formatDueDate(java.time.LocalDateTime dueDate) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd");
            return formatter.format(dueDate);
        }

        private String getInitials(String name) {
            String[] parts = name.trim().split(" ");
            if (parts.length == 0)
                return "";
            if (parts.length == 1)
                return parts[0].substring(0, Math.min(2, parts[0].length()));
            return parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
        }
    }
}
