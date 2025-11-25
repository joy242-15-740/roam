package com.roam.view.components;

import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import com.roam.model.Wiki;
import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.model.Task;
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
    private final ComboBox<Region> regionCombo;
    private final ComboBox<Task> taskCombo;
    private final ComboBox<Wiki> noteCombo;
    private final ComboBox<String> recurrenceCombo;
    private final DatePicker recurrenceEndDatePicker;
    private final Label errorLabel;

    private final CalendarEvent originalEvent;
    private final CalendarEvent workingEvent;
    private final boolean isEditMode;
    private final Runnable onDelete;

    public EventDialog(CalendarEvent event, List<CalendarSource> calendarSources,
            List<Operation> operations, List<Region> regions, List<Task> tasks, List<Wiki> notes, Runnable onDelete) {
        this.originalEvent = event;
        this.isEditMode = event != null && event.getId() != null;
        this.onDelete = onDelete;

        // Use the original event directly for editing, create new for adding
        if (isEditMode) {
            this.workingEvent = event;
            System.out.println("Editing event with ID: " + event.getId());
        } else {
            this.workingEvent = new CalendarEvent();
        }

        setTitle(isEditMode ? "Edit Event" : "Create Event");
        setResizable(true);

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
        regionCombo = createRegionCombo(regions);
        taskCombo = createTaskCombo(tasks);
        noteCombo = createNoteCombo(notes);
        recurrenceCombo = createRecurrenceCombo();
        recurrenceEndDatePicker = new DatePicker();
        errorLabel = createErrorLabel();

        // Setup all-day checkbox
        allDayCheckBox.setFont(Font.font("Poppins", 14));
        allDayCheckBox.setOnAction(e -> {
            boolean allDay = allDayCheckBox.isSelected();
            startHourSpinner.setDisable(allDay);
            startMinuteSpinner.setDisable(allDay);
            endHourSpinner.setDisable(allDay);
            endMinuteSpinner.setDisable(allDay);
        });

        // Setup recurrence combo
        recurrenceEndDatePicker.setPromptText("End date (optional)");
        recurrenceEndDatePicker.setDisable(true);
        recurrenceCombo.setOnAction(e -> {
            boolean isRecurring = !"None".equals(recurrenceCombo.getValue());
            recurrenceEndDatePicker.setDisable(!isRecurring);
        });

        // Create button types
        ButtonType submitButton = new ButtonType(
                isEditMode ? "Save" : "Create",
                ButtonBar.ButtonData.OK_DONE);

        if (isEditMode) {
            ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
            getDialogPane().getButtonTypes().addAll(deleteButton, ButtonType.CANCEL, submitButton);

            Button deleteBtn = (Button) getDialogPane().lookupButton(deleteButton);
            deleteBtn.setStyle("-fx-text-fill: -roam-red;");
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

        // Convert result - this is called when dialog is closed with OK button
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                CalendarEvent result = validateAndCreateEvent();
                if (result != null) {
                    System.out.println(
                            "Event validated successfully. ID: " + result.getId() + ", Title: " + result.getTitle());
                } else {
                    System.out.println("Event validation failed");
                }
                return result;
            }
            return null;
        });

        applyCustomStyling();
    }

    private TextField createTextField(String prompt, int maxLength) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        field.setFont(Font.font("Poppins", 14));
        field.setStyle(
                "-fx-border-color: -roam-border; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: -roam-text-primary; -fx-background-color: -roam-bg-primary;");

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
        area.setFont(Font.font("Poppins", 14));
        area.setStyle(
                "-fx-border-color: -roam-border; -fx-border-radius: 4; -fx-background-radius: 4; -fx-text-fill: -roam-text-primary; -fx-control-inner-background: -roam-bg-primary;");

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
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

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
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        combo.setCellFactory(lv -> new OperationCell());
        combo.setButtonCell(new OperationCell());

        return combo;
    }

    private ComboBox<Region> createRegionCombo(List<Region> regions) {
        ComboBox<Region> combo = new ComboBox<>();
        combo.getItems().addAll(regions);
        combo.setPromptText("Select Region");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        combo.setButtonCell(new RegionListCell());
        combo.setCellFactory(lv -> new RegionListCell());
        return combo;
    }

    private ComboBox<Task> createTaskCombo(List<Task> tasks) {
        ComboBox<Task> combo = new ComboBox<>();
        combo.getItems().addAll(tasks);
        combo.setPromptText("Link to Task");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        combo.setButtonCell(new TaskListCell());
        combo.setCellFactory(lv -> new TaskListCell());
        return combo;
    }

    private ComboBox<Wiki> createNoteCombo(List<Wiki> notes) {
        ComboBox<Wiki> combo = new ComboBox<>();
        combo.getItems().addAll(notes);
        combo.setPromptText("Link to Wiki");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        combo.setButtonCell(new NoteListCell());
        combo.setCellFactory(lv -> new NoteListCell());
        return combo;
    }

    private ComboBox<String> createRecurrenceCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("None", "Daily", "Weekly", "Monthly", "Yearly");
        combo.setValue("None");
        combo.setPrefHeight(40);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        return combo;
    }

    private Spinner<Integer> createTimeSpinner(int initial, int min, int max) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setEditable(true);
        spinner.setPrefWidth(70);
        spinner.setPrefHeight(40);
        spinner.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        return spinner;
    }

    private Label createErrorLabel() {
        Label label = new Label("Title is required");
        label.setFont(Font.font("Poppins", 12));
        label.setStyle("-fx-text-fill: -roam-red;");
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
                createRecurrenceSection(),
                createFieldGroup("Operation", operationCombo),
                createFieldGroup("Region", regionCombo),
                createFieldGroup("Task", taskCombo),
                createFieldGroup("Wiki", noteCombo));

        return layout;
    }

    private VBox createDateTimeSection() {
        VBox section = new VBox(10);

        // Start date/time
        Label startLabel = new Label("Start");
        startLabel.setFont(Font.font("Poppins", 13));
        startLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        HBox startBox = new HBox(10);
        startDatePicker.setPrefWidth(200);
        startDatePicker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        Label startTimeLabel = new Label(":");
        startTimeLabel.setFont(Font.font("Poppins", 14));
        startTimeLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        startBox.getChildren().addAll(startDatePicker, startHourSpinner, startTimeLabel, startMinuteSpinner);

        // End date/time
        Label endLabel = new Label("End");
        endLabel.setFont(Font.font("Poppins", 13));
        endLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        HBox endBox = new HBox(10);
        endDatePicker.setPrefWidth(200);
        endDatePicker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        Label endTimeLabel = new Label(":");
        endTimeLabel.setFont(Font.font("Poppins", 14));
        endTimeLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        endBox.getChildren().addAll(endDatePicker, endHourSpinner, endTimeLabel, endMinuteSpinner);

        section.getChildren().addAll(startLabel, startBox, endLabel, endBox);
        return section;
    }

    private VBox createRecurrenceSection() {
        VBox section = new VBox(10);
        Label label = new Label("Recurrence");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: -roam-text-secondary;");

        HBox box = new HBox(10);
        recurrenceCombo.setPrefWidth(150);
        recurrenceEndDatePicker.setPrefWidth(200);
        recurrenceEndDatePicker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        box.getChildren().addAll(recurrenceCombo, recurrenceEndDatePicker);
        section.getChildren().addAll(label, box);
        return section;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        VBox group = new VBox(5);

        if (!labelText.isEmpty()) {
            Label label = new Label(labelText);
            label.setFont(Font.font("Poppins", 13));
            label.setStyle("-fx-text-fill: -roam-text-secondary;");
            group.getChildren().add(label);
        }

        group.getChildren().add(field);
        return group;
    }

    private void populateFields() {
        if (originalEvent != null) {
            titleField.setText(originalEvent.getTitle());

            if (originalEvent.getStartDateTime() != null) {
                startDatePicker.setValue(originalEvent.getStartDateTime().toLocalDate());
                startHourSpinner.getValueFactory().setValue(originalEvent.getStartDateTime().getHour());
                startMinuteSpinner.getValueFactory().setValue(originalEvent.getStartDateTime().getMinute());
            }

            if (originalEvent.getEndDateTime() != null) {
                endDatePicker.setValue(originalEvent.getEndDateTime().toLocalDate());
                endHourSpinner.getValueFactory().setValue(originalEvent.getEndDateTime().getHour());
                endMinuteSpinner.getValueFactory().setValue(originalEvent.getEndDateTime().getMinute());
            }

            allDayCheckBox.setSelected(originalEvent.getIsAllDay());

            locationField.setText(originalEvent.getLocation());
            descriptionArea.setText(originalEvent.getDescription());

            // Set calendar source
            CalendarSource source = calendarCombo.getItems().stream()
                    .filter(s -> s.getId().equals(originalEvent.getCalendarSourceId()))
                    .findFirst()
                    .orElse(null);
            calendarCombo.setValue(source);

            // Set operation
            if (originalEvent.getOperationId() != null) {
                Operation op = operationCombo.getItems().stream()
                        .filter(o -> o != null && o.getId().equals(originalEvent.getOperationId()))
                        .findFirst()
                        .orElse(null);
                operationCombo.setValue(op);
            }

            if (originalEvent.getRegion() != null) {
                regionCombo.getItems().stream()
                        .filter(r -> r.getName().equals(originalEvent.getRegion()))
                        .findFirst()
                        .ifPresent(regionCombo::setValue);
            }

            if (originalEvent.getTaskId() != null) {
                taskCombo.getItems().stream()
                        .filter(t -> t.getId().equals(originalEvent.getTaskId()))
                        .findFirst()
                        .ifPresent(taskCombo::setValue);
            }

            if (originalEvent.getWikiId() != null) {
                noteCombo.getItems().stream()
                        .filter(n -> n.getId().equals(originalEvent.getWikiId()))
                        .findFirst()
                        .ifPresent(noteCombo::setValue);
            }

            if (originalEvent.getRecurrenceRule() != null) {
                recurrenceCombo.setValue(originalEvent.getRecurrenceRule());
                recurrenceEndDatePicker.setDisable(false);
            }
            if (originalEvent.getRecurrenceEndDate() != null) {
                recurrenceEndDatePicker.setValue(originalEvent.getRecurrenceEndDate().toLocalDate());
            }
        }
    }

    private CalendarEvent validateAndCreateEvent() {
        System.out.println(
                "Validating event... (Edit mode: " + isEditMode + ", Working event ID: " + workingEvent.getId() + ")");
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("Title is required");
            errorLabel.setVisible(true);
            return null;
        }

        CalendarSource selectedCalendar = calendarCombo.getValue();
        if (selectedCalendar == null) {
            errorLabel.setText("Calendar is required");
            errorLabel.setVisible(true);
            return null;
        }

        // Use working copy
        CalendarEvent e = this.workingEvent;

        e.setTitle(title);
        e.setCalendarSourceId(selectedCalendar.getId());
        e.setLocation(locationField.getText().trim());
        e.setDescription(descriptionArea.getText().trim());
        e.setIsAllDay(allDayCheckBox.isSelected());

        // Set dates
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            errorLabel.setText("Start and end dates are required");
            errorLabel.setVisible(true);
            return null;
        }

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
        }

        // Validate end is after start
        if (e.getEndDateTime().isBefore(e.getStartDateTime())) {
            errorLabel.setText("End time must be after start time");
            errorLabel.setVisible(true);
            return null;
        }

        // Set operation
        Operation selectedOp = operationCombo.getValue();
        if (selectedOp != null) {
            e.setOperationId(selectedOp.getId());
        } else {
            e.setOperationId(null);
        }

        if (regionCombo.getValue() != null) {
            e.setRegion(regionCombo.getValue().getName());
        } else {
            e.setRegion(null);
        }

        if (taskCombo.getValue() != null) {
            e.setTaskId(taskCombo.getValue().getId());
        } else {
            e.setTaskId(null);
        }

        if (noteCombo.getValue() != null) {
            e.setWikiId(noteCombo.getValue().getId());
        } else {
            e.setWikiId(null);
        }

        if (!"None".equals(recurrenceCombo.getValue())) {
            e.setRecurrenceRule(recurrenceCombo.getValue());
            if (recurrenceEndDatePicker.getValue() != null) {
                e.setRecurrenceEndDate(recurrenceEndDatePicker.getValue().atTime(23, 59, 59));
            } else {
                e.setRecurrenceEndDate(null);
            }
        } else {
            e.setRecurrenceRule(null);
            e.setRecurrenceEndDate(null);
        }

        System.out
                .println("Event validation passed. Returning event with ID: " + e.getId() + ", Title: " + e.getTitle());
        return e;
    }

    private void applyCustomStyling() {
        DialogPane pane = getDialogPane();

        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-font-family: 'Poppins'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 100px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        Button submitBtn = (Button) pane.lookupButton(pane.getButtonTypes().get(pane.getButtonTypes().size() - 1));
        submitBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-font-family: 'Poppins Bold'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 120px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(66, 133, 244, 0.3), 4, 0, 0, 2);");

        // Add hover effect
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(
                "-fx-background-color: -roam-blue-hover; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-font-family: 'Poppins Bold'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 120px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(66, 133, 244, 0.5), 6, 0, 0, 2);"));

        submitBtn.setOnMouseExited(e -> submitBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-font-family: 'Poppins Bold'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 120px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(66, 133, 244, 0.3), 4, 0, 0, 2);"));
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
                nameLabel.setFont(Font.font("Poppins", 14));

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

    // Custom cell for region combo
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

    // Custom cell for task combo
    private static class TaskListCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getTitle());
            }
        }
    }

    // Custom cell for Wiki combo
    private static class NoteListCell extends ListCell<Wiki> {
        @Override
        protected void updateItem(Wiki item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getTitle());
            }
        }
    }
}
