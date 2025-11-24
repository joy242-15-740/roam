package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class TasksTimelineView extends BorderPane {

    private final TasksController controller;

    // Timeline settings
    private TimeScale currentScale = TimeScale.DAY;
    private LocalDate startDate;
    private LocalDate endDate;
    private int columnWidth = 40;

    // UI components
    private ScrollPane taskListScroll;
    private ScrollPane timelineScroll;
    private VBox taskListContainer;
    private Pane timelineGridContainer;
    private HBox timeAxisHeader;
    private Label scaleLabel;

    // Data
    private List<Task> scheduledTasks = new ArrayList<>();
    private List<Task> unscheduledTasks = new ArrayList<>();

    public enum TimeScale {
        DAY(40, "Day view"),
        WEEK(80, "Week view"),
        MONTH(100, "Month view");

        final int width;
        final String label;

        TimeScale(int width, String label) {
            this.width = width;
            this.label = label;
        }
    }

    public TasksTimelineView(TasksController controller) {
        this.controller = controller;

        this.startDate = LocalDate.now().minusWeeks(2);
        this.endDate = LocalDate.now().plusWeeks(2);

        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: -roam-bg-primary;");

        HBox toolbar = createToolbar();
        setTop(toolbar);

        HBox mainContent = new HBox();

        taskListScroll = createTaskListPane();
        taskListScroll.setPrefWidth(350);
        taskListScroll.setMinWidth(250);

        BorderPane timelinePane = createTimelinePane();
        HBox.setHgrow(timelinePane, javafx.scene.layout.Priority.ALWAYS);

        mainContent.getChildren().addAll(taskListScroll, timelinePane);
        setCenter(mainContent);

        syncScrolling();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPrefHeight(50);
        toolbar.setPadding(new Insets(10, 20, 10, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: -roam-gray-bg; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        Button todayBtn = createToolbarButton("Today");
        todayBtn.setOnAction(e -> goToToday());

        Button zoomOutBtn = createToolbarButton("−");
        zoomOutBtn.setPrefWidth(36);
        zoomOutBtn.setOnAction(e -> zoomOut());

        Button zoomInBtn = createToolbarButton("+");
        zoomInBtn.setPrefWidth(36);
        zoomInBtn.setOnAction(e -> zoomIn());

        scaleLabel = new Label(currentScale.label);
        scaleLabel.setFont(Font.font("Poppins Regular", 13));
        scaleLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        scaleLabel.setPadding(new Insets(0, 10, 0, 10));

        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        toolbar.getChildren().addAll(
                todayBtn,
                zoomOutBtn,
                zoomInBtn,
                scaleLabel,
                separator,
                spacer);

        return toolbar;
    }

    private Button createToolbarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins Regular", 14));
        btn.setPrefWidth(80);
        btn.setPrefHeight(36);
        btn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        return btn;
    }

    private ScrollPane createTaskListPane() {
        taskListContainer = new VBox();
        taskListContainer.setStyle("-fx-background-color: -roam-bg-primary;");

        ScrollPane scroll = new ScrollPane(taskListContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 0 2 0 0;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scroll;
    }

    private BorderPane createTimelinePane() {
        BorderPane pane = new BorderPane();

        timeAxisHeader = createTimeAxisHeader();
        pane.setTop(timeAxisHeader);

        timelineGridContainer = new Pane();
        timelineGridContainer.setStyle("-fx-background-color: -roam-bg-primary;");

        timelineScroll = new ScrollPane(timelineGridContainer);
        timelineScroll.setFitToHeight(true);
        timelineScroll.setStyle("-fx-background-color: -roam-bg-primary;");

        pane.setCenter(timelineScroll);

        return pane;
    }

    private HBox createTimeAxisHeader() {
        HBox header = new HBox();
        header.setPrefHeight(50);
        header.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 0 0 2 0;");

        return header;
    }

    private void syncScrolling() {
        taskListScroll.vvalueProperty().addListener((obs, old, newVal) -> {
            timelineScroll.setVvalue(newVal.doubleValue());
        });

        timelineScroll.vvalueProperty().addListener((obs, old, newVal) -> {
            taskListScroll.setVvalue(newVal.doubleValue());
        });

        timelineScroll.hvalueProperty().addListener((obs, old, newVal) -> {
            timeAxisHeader.setTranslateX(-newVal.doubleValue() *
                    (timelineGridContainer.getWidth() - timelineScroll.getViewportBounds().getWidth()));
        });
    }

    public void loadTasks(List<Task> tasks) {
        scheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        unscheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() == null)
                .collect(Collectors.toList());

        if (scheduledTasks.isEmpty() && unscheduledTasks.isEmpty()) {
            showEmptyState();
        } else if (scheduledTasks.isEmpty()) {
            showOnlyUnscheduled();
        } else {
            renderTimeline();
        }
    }

    private void renderTimeline() {
        taskListContainer.getChildren().clear();
        timelineGridContainer.getChildren().clear();
        timeAxisHeader.getChildren().clear();

        renderTimeAxis();
        renderGridLines();
        renderTodayIndicator();

        int rowIndex = 0;
        for (Task task : scheduledTasks) {
            renderTaskRow(task, rowIndex);
            rowIndex++;
        }

        int totalColumns = calculateColumnCount();
        columnWidth = currentScale.width;

        double timelineWidth = totalColumns * columnWidth;
        double timelineHeight = Math.max(scheduledTasks.size() * 40, 600);

        timelineGridContainer.setPrefSize(timelineWidth, timelineHeight);
        timelineGridContainer.setMinSize(timelineWidth, timelineHeight);

        if (!unscheduledTasks.isEmpty()) {
            renderUnscheduledSection(rowIndex);
        }
    }

    private void renderTimeAxis() {
        timeAxisHeader.getChildren().clear();

        LocalDate current = startDate;

        switch (currentScale) {
            case DAY -> {
                while (!current.isAfter(endDate)) {
                    VBox dayColumn = new VBox();
                    dayColumn.setPrefWidth(columnWidth);
                    dayColumn.setAlignment(Pos.CENTER);
                    dayColumn.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");

                    Label dayLabel = new Label(current.getDayOfMonth() + "");
                    dayLabel.setFont(Font.font("Poppins Bold", 11));
                    dayLabel.setStyle("-fx-text-fill: -roam-text-primary;");

                    Label weekdayLabel = new Label(
                            current.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                    weekdayLabel.setFont(Font.font("Poppins Regular", 10));
                    weekdayLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

                    dayColumn.getChildren().addAll(dayLabel, weekdayLabel);
                    timeAxisHeader.getChildren().add(dayColumn);

                    current = current.plusDays(1);
                }
            }
            case WEEK -> {
                while (!current.isAfter(endDate)) {
                    VBox weekColumn = new VBox();
                    weekColumn.setPrefWidth(columnWidth);
                    weekColumn.setAlignment(Pos.CENTER);
                    weekColumn.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");

                    Label weekLabel = new Label("W" + current.format(DateTimeFormatter.ofPattern("w")));
                    weekLabel.setFont(Font.font("Poppins Bold", 11));
                    weekLabel.setStyle("-fx-text-fill: -roam-text-primary;");

                    LocalDate weekEnd = current.plusDays(6);
                    Label dateRange = new Label(
                            current.format(DateTimeFormatter.ofPattern("MMM d")) + "-" +
                                    weekEnd.format(DateTimeFormatter.ofPattern("d")));
                    dateRange.setFont(Font.font("Poppins Regular", 10));
                    dateRange.setStyle("-fx-text-fill: -roam-text-secondary;");

                    weekColumn.getChildren().addAll(weekLabel, dateRange);
                    timeAxisHeader.getChildren().add(weekColumn);

                    current = current.plusWeeks(1);
                }
            }
            case MONTH -> {
                while (!current.isAfter(endDate)) {
                    VBox monthColumn = new VBox();
                    monthColumn.setPrefWidth(columnWidth);
                    monthColumn.setAlignment(Pos.CENTER);
                    monthColumn.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");

                    Label monthLabel = new Label(
                            current.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
                    monthLabel.setFont(Font.font("Poppins Bold", 11));
                    monthLabel.setStyle("-fx-text-fill: -roam-text-primary;");

                    Label yearLabel = new Label(current.getYear() + "");
                    yearLabel.setFont(Font.font("Poppins Regular", 10));
                    yearLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

                    monthColumn.getChildren().addAll(monthLabel, yearLabel);
                    timeAxisHeader.getChildren().add(monthColumn);

                    current = current.plusMonths(1);
                }
            }
        }
    }

    private void renderGridLines() {
        int totalColumns = calculateColumnCount();
        double height = scheduledTasks.size() * 40;

        for (int i = 0; i <= totalColumns; i++) {
            double x = i * columnWidth;
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.web("#E0E0E0")); // Keep as is or use variable if possible
            line.setStrokeWidth(1);
            timelineGridContainer.getChildren().add(line);
        }

        for (int i = 0; i <= scheduledTasks.size(); i++) {
            double y = i * 40;
            Line line = new Line(0, y, totalColumns * columnWidth, y);
            line.setStroke(Color.web("#F0F0F0")); // Keep as is or use variable if possible
            line.setStrokeWidth(1);
            timelineGridContainer.getChildren().add(line);
        }
    }

    private void renderTodayIndicator() {
        LocalDate today = LocalDate.now();

        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            int columnIndex = calculateColumnIndex(today);
            double x = columnIndex * columnWidth;
            double height = scheduledTasks.size() * 40;

            Line todayLine = new Line(x, 0, x, height);
            todayLine.setStroke(Color.web("#4285f4"));
            todayLine.setStrokeWidth(2);

            timelineGridContainer.getChildren().add(todayLine);
        }
    }

    private void renderTaskRow(Task task, int rowIndex) {
        HBox taskInfo = createTaskInfoBox(task);
        taskInfo.setPrefHeight(40);
        taskListContainer.getChildren().add(taskInfo);

        if (task.getDueDate() != null) {
            StackPane taskBar = createTaskBar(task);

            LocalDate dueDate = task.getDueDate().toLocalDate();
            int columnIndex = calculateColumnIndex(dueDate);

            double x = columnIndex * columnWidth;
            double y = rowIndex * 40 + 6;

            taskBar.setLayoutX(x);
            taskBar.setLayoutY(y);

            timelineGridContainer.getChildren().add(taskBar);
        }
    }

    private HBox createTaskInfoBox(Task task) {
        HBox container = new HBox(8);
        container.setPadding(new Insets(8));
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 0 0 1 0;");

        javafx.scene.shape.Circle priorityDot = new javafx.scene.shape.Circle(4);
        priorityDot.setFill(Color.web(getPriorityColor(task.getPriority())));

        VBox infoBox = new VBox(2);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 13));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Optional<Operation> operation = controller.getOperationById(task.getOperationId());
        if (operation.isPresent()) {
            Label opLabel = new Label(operation.get().getName());
            opLabel.setFont(Font.font("Poppins Regular", 10));
            opLabel.setStyle(
                    "-fx-background-color: -roam-blue-light; " +
                            "-fx-text-fill: -roam-blue; " +
                            "-fx-padding: 2 6 2 6; " +
                            "-fx-background-radius: 8;");
            infoBox.getChildren().add(opLabel);
        }

        infoBox.getChildren().add(0, titleLabel);

        // Edit button
        Button editBtn = new Button();
        FontIcon editIcon = new FontIcon(Feather.EDIT_2);
        editIcon.setIconSize(14);
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
        editBtn.setVisible(false);
        editBtn.setOnAction(e -> {
            e.consume();
            controller.editTask(task);
        });

        container.setOnMouseEntered(e -> {
            container.setStyle(
                    "-fx-background-color: -roam-gray-bg; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 0 0 1 0;");
            editBtn.setVisible(true);
        });

        container.setOnMouseExited(e -> {
            container.setStyle(
                    "-fx-background-color: -roam-bg-primary; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 0 0 1 0;");
            editBtn.setVisible(false);
        });

        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            Label avatar = createAvatarLabel(task.getAssignee());
            container.getChildren().addAll(priorityDot, infoBox, avatar, editBtn);
        } else {
            container.getChildren().addAll(priorityDot, infoBox, editBtn);
        }

        return container;
    }

    private StackPane createTaskBar(Task task) {
        StackPane bar = new StackPane();
        bar.setPrefWidth(columnWidth - 4);
        bar.setPrefHeight(28);
        bar.setMaxWidth(columnWidth - 4);

        Rectangle bg = new Rectangle(columnWidth - 4, 28);
        bg.setArcWidth(4);
        bg.setArcHeight(4);
        bg.setFill(Color.web(getStatusBackgroundColor(task.getStatus()), 0.3));
        bg.setStroke(Color.web(getStatusBorderColor(task.getStatus())));
        bg.setStrokeWidth(1);

        Rectangle priorityBar = new Rectangle(4, 28);
        priorityBar.setFill(Color.web(getPriorityColor(task.getPriority())));
        priorityBar.setArcWidth(4);
        priorityBar.setArcHeight(4);
        StackPane.setAlignment(priorityBar, Pos.CENTER_LEFT);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins Regular", 11));
        titleLabel.setStyle("-fx-text-fill: " + getStatusBorderColor(task.getStatus()) + "; -fx-padding: 0 0 0 8;");
        titleLabel.setMaxWidth(columnWidth - 20);
        StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);

        bar.getChildren().addAll(bg, priorityBar, titleLabel);

        final Rectangle bgRef = bg;
        bar.setOnMouseEntered(e -> {
            bgRef.setFill(Color.web(getStatusBackgroundColor(task.getStatus()), 0.5));
            bar.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);");

            Tooltip tooltip = createTaskTooltip(task);
            Tooltip.install(bar, tooltip);
        });

        bar.setOnMouseExited(e -> {
            bgRef.setFill(Color.web(getStatusBackgroundColor(task.getStatus()), 0.3));
            bar.setStyle("");
        });

        bar.setOnMouseClicked(e -> controller.editTask(task));

        return bar;
    }

    private Tooltip createTaskTooltip(Task task) {
        StringBuilder content = new StringBuilder();
        content.append(task.getTitle()).append("\n\n");

        Optional<Operation> operation = controller.getOperationById(task.getOperationId());
        if (operation.isPresent()) {
            content.append("Operation: ").append(operation.get().getName()).append("\n");
        }

        content.append("Status: ").append(getStatusDisplayName(task.getStatus())).append("\n");
        content.append("Priority: ").append(task.getPriority()).append("\n");

        if (task.getAssignee() != null) {
            content.append("Assigned to: ").append(task.getAssignee()).append("\n");
        }

        if (task.getDueDate() != null) {
            content.append("Due: ").append(
                    task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
        }

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            String desc = task.getDescription();
            if (desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }
            content.append("\n").append(desc);
        }

        Tooltip tooltip = new Tooltip(content.toString());
        tooltip.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-font-family: 'Poppins Regular'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 10; -fx-border-color: -roam-border; -fx-border-width: 1;");
        tooltip.setShowDelay(javafx.util.Duration.millis(500));

        return tooltip;
    }

    private Label createAvatarLabel(String assignee) {
        String initials = getInitials(assignee);
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Poppins Medium", 10));
        avatar.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-min-width: 24; " +
                        "-fx-min-height: 24; " +
                        "-fx-max-width: 24; " +
                        "-fx-max-height: 24; " +
                        "-fx-alignment: center;");
        return avatar;
    }

    private void renderUnscheduledSection(int startRowIndex) {
        HBox unscheduledHeader = new HBox();
        unscheduledHeader.setPrefHeight(36);
        unscheduledHeader.setPadding(new Insets(8));
        unscheduledHeader.setAlignment(Pos.CENTER_LEFT);
        unscheduledHeader.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2 0 1 0;");

        Label headerLabel = new Label("Unscheduled Tasks (" + unscheduledTasks.size() + ")");
        headerLabel.setFont(Font.font("Poppins Bold", 14));
        headerLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        unscheduledHeader.getChildren().add(headerLabel);
        taskListContainer.getChildren().add(unscheduledHeader);

        for (Task task : unscheduledTasks) {
            HBox taskInfo = createTaskInfoBox(task);
            taskListContainer.getChildren().add(taskInfo);
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100));

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 72px;");

        Label title = new Label("No scheduled tasks");
        title.setFont(Font.font("Poppins Bold", 20));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        Label message = new Label("Add due dates to tasks to see them on the timeline");
        message.setFont(Font.font("Poppins Regular", 14));
        message.setStyle("-fx-text-fill: -roam-text-hint;");

        emptyState.getChildren().addAll(icon, title, message);

        taskListContainer.getChildren().add(emptyState);
    }

    private void showOnlyUnscheduled() {
        renderUnscheduledSection(0);
    }

    private int calculateColumnIndex(LocalDate date) {
        switch (currentScale) {
            case DAY -> {
                return (int) startDate.datesUntil(date).count();
            }
            case WEEK -> {
                return (int) (startDate.datesUntil(date).count() / 7);
            }
            case MONTH -> {
                int months = 0;
                LocalDate current = startDate;
                while (current.isBefore(date)) {
                    current = current.plusMonths(1);
                    months++;
                }
                return months;
            }
        }
        return 0;
    }

    private int calculateColumnCount() {
        switch (currentScale) {
            case DAY -> {
                return (int) startDate.datesUntil(endDate.plusDays(1)).count();
            }
            case WEEK -> {
                return (int) Math.ceil(startDate.datesUntil(endDate.plusDays(1)).count() / 7.0);
            }
            case MONTH -> {
                int months = 0;
                LocalDate current = startDate;
                while (!current.isAfter(endDate)) {
                    current = current.plusMonths(1);
                    months++;
                }
                return months;
            }
        }
        return 0;
    }

    private void zoomIn() {
        switch (currentScale) {
            case MONTH -> currentScale = TimeScale.WEEK;
            case WEEK -> currentScale = TimeScale.DAY;
            case DAY -> {
                return;
            }
        }
        scaleLabel.setText(currentScale.label);
        renderTimeline();
    }

    private void zoomOut() {
        switch (currentScale) {
            case DAY -> currentScale = TimeScale.WEEK;
            case WEEK -> currentScale = TimeScale.MONTH;
            case MONTH -> {
                return;
            }
        }
        scaleLabel.setText(currentScale.label);
        renderTimeline();
    }

    private void goToToday() {
        startDate = LocalDate.now().minusWeeks(2);
        endDate = LocalDate.now().plusWeeks(2);
        renderTimeline();
    }

    private String getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> "#C62828";
            case MEDIUM -> "#F9A825";
            case LOW -> "#616161";
        };
    }

    private String getStatusBackgroundColor(TaskStatus status) {
        return switch (status) {
            case TODO -> "#E3F2FD";
            case IN_PROGRESS -> "#FFF3E0";
            case DONE -> "#E8F5E9";
        };
    }

    private String getStatusBorderColor(TaskStatus status) {
        return switch (status) {
            case TODO -> "#1976D2";
            case IN_PROGRESS -> "#F57C00";
            case DONE -> "#388E3C";
        };
    }

    private String getStatusDisplayName(TaskStatus status) {
        return switch (status) {
            case TODO -> "To Do";
            case IN_PROGRESS -> "In Progress";
            case DONE -> "Done";
        };
    }

    private String getInitials(String name) {
        String[] parts = name.trim().split(" ");
        if (parts.length == 0)
            return "";
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
