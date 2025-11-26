package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.model.Region;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.util.List;

public class OperationDialog extends Dialog<Operation> {

    private final TextField nameField;
    private final TextArea purposeField;
    private final DatePicker dueDatePicker;
    private final ComboBox<OperationStatus> statusCombo;
    private final ComboBox<Priority> priorityCombo;
    private final ComboBox<Region> regionCombo;
    private final TextArea outcomeField;
    private final Label errorLabel;

    private final Operation operation;
    private final boolean isEditMode;

    public OperationDialog(Operation operation, List<Region> regions) {
        this.operation = operation;
        this.isEditMode = operation != null;

        // Set dialog title
        setTitle(isEditMode ? "Edit Operation" : "Create New Operation");
        setResizable(true);

        // Create form fields
        nameField = createTextField("Enter operation name", 255);
        purposeField = createTextArea("Describe the purpose of this operation", 1000);
        dueDatePicker = createDatePicker();
        statusCombo = createStatusComboBox();
        priorityCombo = createPriorityComboBox();
        regionCombo = createRegionComboBox(regions);
        outcomeField = createTextArea("Enter operation outcome (optional)", 1000);
        errorLabel = createErrorLabel();

        // Set button types
        ButtonType submitButton = new ButtonType(
                isEditMode ? "Update" : "Create",
                ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        // Create form layout
        VBox content = createFormLayout();

        // Wrap content in ScrollPane for scrollability
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxHeight(650);

        getDialogPane().setContent(scrollPane);

        // Set dialog width and max height for screen-fit
        getDialogPane().setPrefWidth(550);
        getDialogPane().setMaxHeight(700);

        // Pre-fill data if editing
        if (isEditMode) {
            populateFields();
        }

        // Disable submit button if name is empty
        Button submitBtn = (Button) getDialogPane().lookupButton(submitButton);
        submitBtn.setDisable(true);
        nameField.textProperty().addListener((obs, old, newVal) -> {
            submitBtn.setDisable(newVal == null || newVal.trim().isEmpty());
            if (!newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        // Enable submit button if editing with pre-filled name
        if (isEditMode && nameField.getText() != null && !nameField.getText().trim().isEmpty()) {
            submitBtn.setDisable(false);
        }

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return validateAndCreateOperation();
            }
            return null;
        });

        // Apply custom styling
        applyCustomStyling();
    }

    private TextField createTextField(String prompt, int maxLength) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setFont(Font.font("Poppins", 14));
        field.setStyle(
                "-fx-border-color: -roam-border; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: -roam-text-primary; -fx-background-color: -roam-bg-primary;");

        // Limit character count
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
        area.setPrefHeight(100);
        area.setWrapText(true);
        area.setFont(Font.font("Poppins", 14));
        area.setStyle(
                "-fx-border-color: -roam-border; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: -roam-text-primary; -fx-control-inner-background: -roam-bg-primary;");

        // Limit character count
        area.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                area.setText(old);
            }
        });

        return area;
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPromptText("Select due date");
        picker.setPrefHeight(40);
        picker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        return picker;
    }

    private ComboBox<OperationStatus> createStatusComboBox() {
        ComboBox<OperationStatus> combo = new ComboBox<>();
        combo.getItems().addAll(OperationStatus.values());
        combo.setValue(OperationStatus.ONGOING);
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        // Custom display
        combo.setButtonCell(new StatusListCell());
        combo.setCellFactory(lv -> new StatusListCell());

        return combo;
    }

    private ComboBox<Priority> createPriorityComboBox() {
        ComboBox<Priority> combo = new ComboBox<>();
        combo.getItems().addAll(Priority.values());
        combo.setValue(Priority.MEDIUM);
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        // Custom display
        combo.setButtonCell(new PriorityListCell());
        combo.setCellFactory(lv -> new PriorityListCell());

        return combo;
    }

    private ComboBox<Region> createRegionComboBox(List<Region> regions) {
        ComboBox<Region> combo = new ComboBox<>();
        combo.getItems().addAll(regions);
        combo.setPromptText("Select Region");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        combo.setButtonCell(new RegionListCell());
        combo.setCellFactory(lv -> new RegionListCell());

        return combo;
    }

    private Label createErrorLabel() {
        Label label = new Label("Name is required");
        label.setFont(Font.font("Poppins", 12));
        label.setStyle("-fx-text-fill: -roam-red;");
        label.setVisible(false);
        return label;
    }

    private VBox createFormLayout() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(25));

        layout.getChildren().addAll(
                createFieldGroup("Name *", nameField),
                errorLabel,
                createFieldGroup("Purpose", purposeField),
                createFieldGroup("Region", regionCombo),
                createFieldGroup("Due Date", dueDatePicker),
                createFieldGroup("Status *", statusCombo),
                createFieldGroup("Priority *", priorityCombo),
                createFieldGroup("Outcome", outcomeField));

        return layout;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        VBox group = new VBox(5);

        Label label = new Label(labelText);
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: -roam-text-secondary;");

        group.getChildren().addAll(label, field);
        return group;
    }

    private void populateFields() {
        if (operation != null) {
            nameField.setText(operation.getName());
            purposeField.setText(operation.getPurpose());
            dueDatePicker.setValue(operation.getDueDate());
            statusCombo.setValue(operation.getStatus());
            priorityCombo.setValue(operation.getPriority());
            outcomeField.setText(operation.getOutcome());

            if (operation.getRegion() != null) {
                regionCombo.getItems().stream()
                        .filter(r -> r.getName().equals(operation.getRegion()))
                        .findFirst()
                        .ifPresent(regionCombo::setValue);
            }
        }
    }

    private Operation validateAndCreateOperation() {
        // Validate name
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setVisible(true);
            return null;
        }

        // Create or update operation
        Operation op = isEditMode ? operation : new Operation();

        op.setName(name);
        op.setPurpose(purposeField.getText().trim());
        op.setDueDate(dueDatePicker.getValue());
        op.setStatus(statusCombo.getValue());
        op.setPriority(priorityCombo.getValue());
        op.setOutcome(outcomeField.getText().trim());

        if (regionCombo.getValue() != null) {
            op.setRegion(regionCombo.getValue().getName());
        } else {
            op.setRegion(null);
        }

        return op;
    }

    private void applyCustomStyling() {
        // Apply theme-aware styling
        ThemeManager.getInstance().styleDialog(this);

        DialogPane pane = getDialogPane();
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#2d2d2d" : "#f5f5f5";
        String textColor = isDark ? "#ffffff" : "#212121";

        pane.lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-family: 'Poppins'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 100px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        pane.lookupButton(pane.getButtonTypes().get(0)).setStyle(
                "-fx-background-color: #4285f4; " +
                        "-fx-text-fill: #ffffff; " +
                        "-fx-font-family: 'Poppins'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 100px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
    }

    // Custom cell for Status ComboBox
    private static class StatusListCell extends ListCell<OperationStatus> {
        @Override
        protected void updateItem(OperationStatus item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case ONGOING -> setText("Ongoing");
                    case IN_PROGRESS -> setText("In Progress");
                    case END -> setText("Completed");
                }
            }
        }
    }

    // Custom cell for Priority ComboBox
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

    // Custom cell for Region ComboBox
    private static class RegionListCell extends ListCell<Region> {
        @Override
        protected void updateItem(Region item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                setStyle("-fx-text-fill: " + item.getColor() + ";");
            }
        }
    }
}
