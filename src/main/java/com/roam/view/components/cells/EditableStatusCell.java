package com.roam.view.components.cells;

import com.roam.controller.TasksController;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class EditableStatusCell extends TableCell<Task, TaskStatus> {

    private final TasksController controller;
    private ComboBox<TaskStatus> comboBox;

    public EditableStatusCell(TasksController controller) {
        this.controller = controller;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox();
            setText(null);
            setGraphic(comboBox);
            comboBox.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(null);
        setGraphic(createStatusBadge(getItem()));
    }

    @Override
    public void updateItem(TaskStatus status, boolean empty) {
        super.updateItem(status, empty);

        if (empty || status == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                    comboBox.setValue(status);
                }
                setText(null);
                setGraphic(comboBox);
            } else {
                setText(null);
                setGraphic(createStatusBadge(status));
            }
        }
    }

    private void createComboBox() {
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll(TaskStatus.values());
        comboBox.setValue(getItem());
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // Custom cell factory for ComboBox items
        comboBox.setCellFactory(param -> new javafx.scene.control.ListCell<TaskStatus>() {
            @Override
            protected void updateItem(TaskStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(createStatusBadge(status));
                }
            }
        });

        // Custom button cell for selected value
        comboBox.setButtonCell(new javafx.scene.control.ListCell<TaskStatus>() {
            @Override
            protected void updateItem(TaskStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(createStatusBadge(status));
                }
            }
        });

        comboBox.setOnAction(event -> {
            TaskStatus newStatus = comboBox.getValue();
            if (newStatus != null && getTableRow() != null && getTableRow().getItem() != null) {
                Task task = getTableRow().getItem();
                controller.updateTaskStatus(task, newStatus);
                commitEdit(newStatus);
            }
        });

        comboBox.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    cancelEdit();
                    break;
                case ENTER:
                    commitEdit(comboBox.getValue());
                    break;
                default:
                    break;
            }
        });
    }

    private HBox createStatusBadge(TaskStatus status) {
        HBox badge = new HBox();
        badge.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setMaxWidth(120);

        Label label = new Label();
        label.setFont(Font.font("Poppins Medium", 12));

        switch (status) {
            case TODO:
                label.setText("⭕ To Do");
                badge.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: #E65100;");
                break;
            case IN_PROGRESS:
                label.setText("⏳ In Progress");
                badge.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: #1565C0;");
                break;
            case DONE:
                label.setText("✓ Done");
                badge.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: #2E7D32;");
                break;
        }

        badge.getChildren().add(label);
        return badge;
    }
}
