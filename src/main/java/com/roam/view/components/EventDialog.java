package com.roam.view.components;

import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import com.roam.model.Operation;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EventDialog extends Dialog<CalendarEvent> {

    private final TextField titleField;
    private final ComboBox<CalendarSource> calendarCombo;
    private final DatePicker startDatePicker;
    private final Spinner<Integer> startHourSpinner;
    private final Spinner<Integer> startMinuteSpinner;
    private final DatePicker endDatePicker;
    private final Spinner<Integer> endHourSpinner;
    private final Spinner<Integer> endMinuteSpinner;
    private final CheckBox allDayCheckBox;
    private final TextField locationField;
    private final TextArea descriptionArea;
    private final ComboBox<Operation> operationCombo;
    private final Label errorLabel;

    private final CalendarEvent event;
    private final boolean isEditMode;
    private final Runnable onDelete;

    public EventDialog(CalendarEvent event, List<CalendarSource> calendarSources,
            List<Operation> operations, Runnable onDelete) {
        this.event = event;
        this.isEditMode = event != null && event.getId() != null;
        this.onDelete = onDelete;

        setTitle(isEditMode ? "Edit Event" : "Create Event");
        setResizable(false);

        // Create form fields
        titleField = createTextField("Add title", 255);
        calendarCombo = createCalendarCombo(calendarSources);
        startDatePicker = new DatePicker(LocalDate.now());
        startHourSpinner = createTimeSpinner(9, 0, 23);
        startMinuteSpinner = createTimeSpinner(0, 0, 59);
        endDatePicker = new DatePicker(LocalDate.now());
        endHourSpinner = createTimeSpinner(10, 0, 23);
        endMinuteSpinner = createTimeSpinner(0, 0, 59);
        allDayCheckBox = new CheckBox("All day");
        locationField = createTextField("Add location", 255);
        descriptionArea = createTextArea("Add description", 1000);
        operationCombo = createOperationCombo(operations);
        errorLabel = createErrorLabel();

        // Setup all-day checkbox
        allDayCheckBox.setFont(Font.font("Poppins Regular", 14));
        allDayCheckBox.setOnAction(e -> {
            boolean allDay = allDayCheckBox.isSelected();
            startHourSpinner.setDisable(allDay);
            startMinuteSpinner.setDisable(allDay);
            endHourSpinner.setDisable(allDay);
            endMinuteSpinner.setDisable(allDay);
        });

        // Create button types
        ButtonType submitButton = new ButtonType(
                isEditMode ? "Save" : "Create",
                ButtonBar.ButtonData.OK_DONE);

        if (isEditMode) {
            ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
            getDialogPane().getButtonTypes().addAll(deleteButton, ButtonType.CANCEL, submitButton);

            Button deleteBtn = (Button) getDialogPane().lookupButton(deleteButton);
            deleteBtn.setStyle("-fx-text-fill: #C62828;");
            deleteBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                e.consume();
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

        // Wrap content in ScrollPane
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(500); // Limit height
        scrollPane.setMaxHeight(550);

        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefWidth(600);
        getDialogPane().setMaxHeight(650);

        // Pre-fill data if editing
        if (isEditMode) {
            populateFields();
        }

        // Validation
        Button submitBtn = (Button) getDialogPane().lookupButton(submitButton);
        submitBtn.setDisable(!isEditMode && (titleField.getText() == null || titleField.getText().trim().isEmpty()));

        titleField.textProperty().addListener((obs, old, newVal) -> {
            submitBtn.setDisable(newVal == null || newVal.trim().isEmpty());
            if (newVal != null && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return validateAndCreateEvent();
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

    private ComboBox<CalendarSource> createCalendarCombo(List<CalendarSource> sources) {
        ComboBox<CalendarSource> combo = new ComboBox<>();
        combo.getItems().addAll(sources);

        if (!sources.isEmpty()) {
            // Set default calendar
            CalendarSource defaultSource = sources.stream()
                    .filter(s -> s.getIsDefault())
                    .findFirst()
                    .orElse(sources.get(0));
            combo.setValue(defaultSource);
        }

        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        combo.setCellFactory(lv -> new CalendarSourceCell());
        combo.setButtonCell(new CalendarSourceCell());

        return combo;
    }

    private ComboBox<Operation> createOperationCombo(List<Operation> operations) {
        ComboBox<Operation> combo = new ComboBox<>();
        combo.getItems().add(null); // Allow no selection
        combo.getItems().addAll(operations);
        combo.setValue(null);
        combo.setPromptText("Link to operation (optional)");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        combo.setCellFactory(lv -> new OperationCell());
        combo.setButtonCell(new OperationCell());

        return combo;
    }

    private Spinner<Integer> createTimeSpinner(int initial, int min, int max) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setEditable(true);
        spinner.setPrefWidth(70);
        spinner.setPrefHeight(40);
        spinner.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");
        return spinner;
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
                createFieldGroup("Calendar *", calendarCombo),
                createDateTimeSection(),
                createFieldGroup("", allDayCheckBox),
                createFieldGroup("Location", locationField),
                createFieldGroup("Description", descriptionArea),
                createFieldGroup("Operation", operationCombo));

        return layout;
    }

    private VBox createDateTimeSection() {
        VBox section = new VBox(10);

        // Start date/time
        Label startLabel = new Label("Start");
        startLabel.setFont(Font.font("Poppins Regular", 13));
        startLabel.setStyle("-fx-text-fill: #616161;");

        HBox startBox = new HBox(10);
        startDatePicker.setPrefWidth(200);
        startDatePicker.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");
        Label startTimeLabel = new Label(":");
        startTimeLabel.setFont(Font.font("Poppins Regular", 14));
        startBox.getChildren().addAll(startDatePicker, startHourSpinner, startTimeLabel, startMinuteSpinner);

        // End date/time
        Label endLabel = new Label("End");
        endLabel.setFont(Font.font("Poppins Regular", 13));
        endLabel.setStyle("-fx-text-fill: #616161;");

        HBox endBox = new HBox(10);
        endDatePicker.setPrefWidth(200);
        endDatePicker.setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");
        Label endTimeLabel = new Label(":");
        endTimeLabel.setFont(Font.font("Poppins Regular", 14));
        endBox.getChildren().addAll(endDatePicker, endHourSpinner, endTimeLabel, endMinuteSpinner);

        section.getChildren().addAll(startLabel, startBox, endLabel, endBox);
        return section;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        VBox group = new VBox(5);

        if (!labelText.isEmpty()) {
            Label label = new Label(labelText);
            label.setFont(Font.font("Poppins Regular", 13));
            label.setStyle("-fx-text-fill: #616161;");
            group.getChildren().add(label);
        }

        group.getChildren().add(field);
        return group;
    }

    private void populateFields() {
        if (event != null) {
            titleField.setText(event.getTitle());

            if (event.getStartDateTime() != null) {
                startDatePicker.setValue(event.getStartDateTime().toLocalDate());
                startHourSpinner.getValueFactory().setValue(event.getStartDateTime().getHour());
                startMinuteSpinner.getValueFactory().setValue(event.getStartDateTime().getMinute());
            }

            if (event.getEndDateTime() != null) {
                endDatePicker.setValue(event.getEndDateTime().toLocalDate());
                endHourSpinner.getValueFactory().setValue(event.getEndDateTime().getHour());
                endMinuteSpinner.getValueFactory().setValue(event.getEndDateTime().getMinute());
            }

            allDayCheckBox.setSelected(event.getIsAllDay());

            locationField.setText(event.getLocation());
            descriptionArea.setText(event.getDescription());

            // Set calendar source
            CalendarSource source = calendarCombo.getItems().stream()
                    .filter(s -> s.getId().equals(event.getCalendarSourceId()))
                    .findFirst()
                    .orElse(null);
            calendarCombo.setValue(source);

            // Set operation
            if (event.getOperationId() != null) {
                Operation op = operationCombo.getItems().stream()
                        .filter(o -> o != null && o.getId().equals(event.getOperationId()))
                        .findFirst()
                        .orElse(null);
                operationCombo.setValue(op);
            }
        }
    }

    private CalendarEvent validateAndCreateEvent() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setVisible(true);
            return null;
        }

        CalendarSource selectedCalendar = calendarCombo.getValue();
        if (selectedCalendar == null) {
            errorLabel.setText("Calendar is required");
            errorLabel.setVisible(true);
            return null;
        }

        CalendarEvent e = isEditMode ? event : new CalendarEvent();

        e.setTitle(title);
        e.setCalendarSourceId(selectedCalendar.getId());
        e.setLocation(locationField.getText().trim());
        e.setDescription(descriptionArea.getText().trim());
        e.setIsAllDay(allDayCheckBox.isSelected());

        // Set dates
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (allDayCheckBox.isSelected()) {
            e.setStartDateTime(startDate.atStartOfDay());
            e.setEndDateTime(endDate.atTime(23, 59, 59));
        } else {
            LocalTime startTime = LocalTime.of(
                    startHourSpinner.getValue(),
                    startMinuteSpinner.getValue());
            LocalTime endTime = LocalTime.of(
                    endHourSpinner.getValue(),
                    endMinuteSpinner.getValue());

            e.setStartDateTime(LocalDateTime.of(startDate, startTime));
            e.setEndDateTime(LocalDateTime.of(endDate, endTime));

            // Validate end is after start
            if (e.getEndDateTime().isBefore(e.getStartDateTime())) {
                errorLabel.setText("End time must be after start time");
                errorLabel.setVisible(true);
                return null;
            }
        }

        // Set operation
        Operation selectedOp = operationCombo.getValue();
        if (selectedOp != null) {
            e.setOperationId(selectedOp.getId());
        } else {
            e.setOperationId(null);
        }

        return e;
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

    // Custom cell for calendar source combo
    private static class CalendarSourceCell extends ListCell<CalendarSource> {
        @Override
        protected void updateItem(CalendarSource item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox container = new HBox(8);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Color indicator
                javafx.scene.shape.Circle colorDot = new javafx.scene.shape.Circle(5);
                colorDot.setFill(javafx.scene.paint.Color.web(item.getColor()));

                Label nameLabel = new Label(item.getName());
                nameLabel.setFont(Font.font("Poppins Regular", 14));

                container.getChildren().addAll(colorDot, nameLabel);
                setGraphic(container);
                setText(null);
            }
        }
    }

    // Custom cell for operation combo
    private static class OperationCell extends ListCell<Operation> {
        @Override
        protected void updateItem(Operation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
            } else if (item == null) {
                setText("None");
            } else {
                setText(item.getName());
            }
        }
    }
}
