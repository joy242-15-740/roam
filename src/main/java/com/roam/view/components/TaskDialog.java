package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TaskDialog extends Dialog<Task> {

    private final TextField titleField;
    private final TextArea descriptionField;
    private final ComboBox<TaskStatus> statusCombo;
    private final ComboBox<Priority> priorityCombo;
    private final ComboBox<Operation> operationCombo;
    private final DatePicker dueDatePicker;
    private final Label errorLabel;

    private final Task task;
    private final boolean isEditMode;
    private final Runnable onDelete;
    private final List<Operation> operations;

    // Constructor for operation-specific tasks (existing behavior)
    public TaskDialog(Task task, Runnable onDelete) {
        this(task, onDelete, null);
    }

    // Constructor for global task creation with operation selector
    public TaskDialog(Task task, Runnable onDelete, List<Operation> operations) {
        this.task = task;
        this.isEditMode = task != null && task.getId() != null;
        this.onDelete = onDelete;
        this.operations = operations;

        setTitle(isEditMode ? "Edit Task" : "Create New Task");
        setResizable(false);

        // Create form fields
        titleField = createTextField("Enter task title", 255);
        descriptionField = createTextArea("Enter task description (optional)", 1000);
        statusCombo = createStatusComboBox();
        priorityCombo = createPriorityComboBox();
        operationCombo = operations != null ? createOperationComboBox(operations) : null;
        dueDatePicker = createDatePicker();
        errorLabel = createErrorLabel();

        // Create button types
        ButtonType submitButton = new ButtonType(
                isEditMode ? "Update" : "Create",
                ButtonBar.ButtonData.OK_DONE);

        if (isEditMode) {
            ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
            getDialogPane().getButtonTypes().addAll(deleteButton, ButtonType.CANCEL, submitButton);

            // Handle delete button
            Button deleteBtn = (Button) getDialogPane().lookupButton(deleteButton);
            deleteBtn.setStyle("-fx-text-fill: #C62828;");
            deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                event.consume();
                if (onDelete != null) {
                    onDelete.run();
                    close();
                }
            });
        } else {
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, submitButton);
        }

        // Create form layout
        VBox content = createFormLayout();
        getDialogPane().setContent(content);
        getDialogPane().setPrefWidth(500);

        // Pre-fill data if editing
        if (isEditMode) {
            populateFields();
        }

        // Validation
        Button submitBtn = (Button) getDialogPane().lookupButton(submitButton);
        // In edit mode or when no operation selector, enable button if title is filled
        submitBtn.setDisable(!isEditMode && (titleField.getText() == null || titleField.getText().trim().isEmpty()));

        titleField.textProperty().addListener((obs, old, newVal) -> {
            boolean titleEmpty = newVal == null || newVal.trim().isEmpty();
            boolean operationEmpty = operationCombo != null && operationCombo.getValue() == null;
            submitBtn.setDisable(titleEmpty || operationEmpty);
            if (newVal != null && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        if (operationCombo != null) {
            operationCombo.valueProperty().addListener((obs, old, newVal) -> {
                boolean titleEmpty = titleField.getText() == null || titleField.getText().trim().isEmpty();
                boolean operationEmpty = newVal == null;
                submitBtn.setDisable(titleEmpty || operationEmpty);
            });
        }

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return validateAndCreateTask();
            }
            return null;
        });

        applyCustomStyling();
    }

    private TextField createTextField(String prompt, int maxLength) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setFont(Font.font("Poppins Regular", 14));
        field.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 4; -fx-background-radius: 4;");

        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                field.setText(old);
            }
        });

        return field;
    }

    private TextArea createTextArea(String prompt, int maxLength) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefHeight(80);
        area.setWrapText(true);
        area.setFont(Font.font("Poppins Regular", 14));
        area.setStyle("-fx-border-color: #E0E0E0; -fx-border-radius: 4; -fx-background-radius: 4;");

        area.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                area.setText(old);
            }
        });

        return area;
    }

    private ComboBox<TaskStatus> createStatusComboBox() {
        ComboBox<TaskStatus> combo = new ComboBox<>();
        combo.getItems().addAll(TaskStatus.values());
        combo.setValue(TaskStatus.TODO);
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        combo.setButtonCell(new TaskStatusListCell());
        combo.setCellFactory(lv -> new TaskStatusListCell());

        return combo;
    }

    private ComboBox<Priority> createPriorityComboBox() {
        ComboBox<Priority> combo = new ComboBox<>();
        combo.getItems().addAll(Priority.values());
        combo.setValue(Priority.MEDIUM);
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        combo.setButtonCell(new PriorityListCell());
        combo.setCellFactory(lv -> new PriorityListCell());

        return combo;
    }

    private ComboBox<Operation> createOperationComboBox(List<Operation> ops) {
        ComboBox<Operation> combo = new ComboBox<>();
        combo.getItems().addAll(ops);
        combo.setPromptText("Select operation");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        combo.setButtonCell(new OperationListCell());
        combo.setCellFactory(lv -> new OperationListCell());

        return combo;
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPromptText("Select due date");
        picker.setPrefHeight(40);
        picker.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");
        return picker;
    }

    private Label createErrorLabel() {
        Label label = new Label("Title is required");
        label.setFont(Font.font("Poppins Regular", 12));
        label.setStyle("-fx-text-fill: #C62828;");
        label.setVisible(false);
        return label;
    }

    private VBox createFormLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));

        layout.getChildren().addAll(
                createFieldGroup("Title *", titleField),
                errorLabel,
                createFieldGroup("Description", descriptionField));

        // Add operation selector if available
        if (operationCombo != null) {
            layout.getChildren().add(createFieldGroup("Operation *", operationCombo));
        }

        layout.getChildren().addAll(
                createFieldGroup("Status *", statusCombo),
                createFieldGroup("Priority *", priorityCombo),
                createFieldGroup("Due Date", dueDatePicker));

        return layout;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        VBox group = new VBox(5);

        Label label = new Label(labelText);
        label.setFont(Font.font("Poppins Regular", 13));
        label.setStyle("-fx-text-fill: #616161;");

        group.getChildren().addAll(label, field);
        return group;
    }

    private void populateFields() {
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionField.setText(task.getDescription());
            statusCombo.setValue(task.getStatus());
            priorityCombo.setValue(task.getPriority());
            if (operationCombo != null && task.getOperationId() != null) {
                operationCombo.getItems().stream()
                        .filter(op -> op.getId().equals(task.getOperationId()))
                        .findFirst()
                        .ifPresent(operationCombo::setValue);
            }
            if (task.getDueDate() != null) {
                dueDatePicker.setValue(task.getDueDate().toLocalDate());
            }
        }
    }

    private Task validateAndCreateTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setVisible(true);
            return null;
        }

        Task t = isEditMode ? task : new Task();

        t.setTitle(title);
        t.setDescription(descriptionField.getText().trim());
        t.setStatus(statusCombo.getValue());
        t.setPriority(priorityCombo.getValue());

        // Set operation ID if operation selector is present
        if (operationCombo != null && operationCombo.getValue() != null) {
            t.setOperationId(operationCombo.getValue().getId());
        }

        if (dueDatePicker.getValue() != null) {
            // Combine date with end of day time
            t.setDueDate(LocalDateTime.of(dueDatePicker.getValue(), LocalTime.of(23, 59)));
        } else {
            t.setDueDate(null);
        }

        return t;
    }

    private void applyCustomStyling() {
        DialogPane pane = getDialogPane();
        pane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #F5F5F5; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-font-family: 'Poppins Regular'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 100px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        pane.lookupButton(pane.getButtonTypes().get(pane.getButtonTypes().size() - 1)).setStyle(
                "-fx-background-color: #4285f4; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Poppins Regular'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 100px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
    }

    private static class TaskStatusListCell extends ListCell<TaskStatus> {
        @Override
        protected void updateItem(TaskStatus item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case TODO -> setText("To Do");
                    case IN_PROGRESS -> setText("In Progress");
                    case DONE -> setText("Done");
                }
            }
        }
    }

    private static class PriorityListCell extends ListCell<Priority> {
        @Override
        protected void updateItem(Priority item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case HIGH -> setText("High");
                    case MEDIUM -> setText("Medium");
                    case LOW -> setText("Low");
                }
            }
        }
    }

    private static class OperationListCell extends ListCell<Operation> {
        @Override
        protected void updateItem(Operation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getName());
            }
        }
    }
}
