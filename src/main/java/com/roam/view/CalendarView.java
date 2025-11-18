package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarView extends BorderPane {

    public enum ViewType {
        MONTH, WEEK, DAY
    }

    private final CalendarController controller;

    private Label dateLabel;
    private VBox filterPanel;
    private StackPane calendarContainer;

    private ViewType currentViewType = ViewType.DAY;
    private YearMonth currentYearMonth;
    private LocalDate currentDate;
    private ToggleGroup viewToggleGroup;

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    public CalendarView(CalendarController controller) {
        this.controller = controller;
        this.currentYearMonth = YearMonth.now();
        this.currentDate = LocalDate.now();

        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: #FFFFFF;");

        // Create toolbar
        HBox toolbar = createToolbar();
        setTop(toolbar);

        // Create filter panel
        filterPanel = createFilterPanel();

        // Create calendar container
        calendarContainer = new StackPane();
        calendarContainer.setPadding(new Insets(20));

        setCenter(calendarContainer);

        // Load data
        controller.setOnDataChanged(this::refreshCalendar);
        refreshCalendar();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPrefHeight(60);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        // Today button
        Button todayBtn = createToolbarButton("Today");
        todayBtn.setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            currentDate = LocalDate.now();
            refreshCalendar();
        });

        // Previous button
        Button prevBtn = createToolbarButton("◄");
        prevBtn.setPrefWidth(36);
        prevBtn.setOnAction(e -> navigatePrevious());

        // Next button
        Button nextBtn = createToolbarButton("►");
        nextBtn.setPrefWidth(36);
        nextBtn.setOnAction(e -> navigateNext());

        // Date label
        dateLabel = new Label(getDateLabelText());
        dateLabel.setFont(Font.font("Poppins Bold", 18));
        dateLabel.setStyle("-fx-text-fill: #000000;");
        dateLabel.setMinWidth(250);
        dateLabel.setAlignment(Pos.CENTER);

        // View selector
        HBox viewSelector = createViewSelector();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // New Event button
        Button newEventBtn = createToolbarButton("+ New Event");
        newEventBtn.setStyle(
                "-fx-background-color: #4285f4; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Poppins Regular'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-min-width: 120px; " +
                        "-fx-min-height: 36px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        newEventBtn.setOnAction(e -> controller.createEvent(currentDate));

        // Filter toggle button
        Button filterBtn = createToolbarButton("☰");
        filterBtn.setPrefWidth(36);
        filterBtn.setOnAction(e -> toggleFilterPanel());

        toolbar.getChildren().addAll(
                todayBtn, prevBtn, nextBtn, dateLabel, viewSelector, spacer, newEventBtn, filterBtn);

        return toolbar;
    }

    private Button createToolbarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins Regular", 14));
        btn.setMinWidth(80);
        btn.setPrefHeight(36);
        btn.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #F5F5F5; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        return btn;
    }

    private HBox createViewSelector() {
        HBox selector = new HBox(0);
        selector.setAlignment(Pos.CENTER);

        viewToggleGroup = new ToggleGroup();

        ToggleButton monthBtn = createViewToggleButton("Month", ViewType.MONTH, true);
        ToggleButton weekBtn = createViewToggleButton("Week", ViewType.WEEK, false);
        ToggleButton dayBtn = createViewToggleButton("Day", ViewType.DAY, false);

        monthBtn.setToggleGroup(viewToggleGroup);
        weekBtn.setToggleGroup(viewToggleGroup);
        dayBtn.setToggleGroup(viewToggleGroup);

        dayBtn.setSelected(true);

        selector.getChildren().addAll(monthBtn, weekBtn, dayBtn);
        return selector;
    }

    private ToggleButton createViewToggleButton(String text, ViewType viewType, boolean isFirst) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Font.font("Poppins Regular", 13));
        btn.setMinWidth(70);
        btn.setPrefHeight(36);

        String baseStylePrefix = "-fx-background-color: #FFFFFF; " +
                "-fx-text-fill: #616161; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1; " +
                "-fx-cursor: hand;";

        String selectedStylePrefix = "-fx-background-color: #E3F2FD; " +
                "-fx-text-fill: #4285f4; " +
                "-fx-border-color: #4285f4; " +
                "-fx-border-width: 1; " +
                "-fx-cursor: hand;";

        String radiusSuffix;
        if (isFirst) {
            radiusSuffix = " -fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;";
        } else if (viewType == ViewType.DAY) {
            radiusSuffix = " -fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;";
        } else {
            radiusSuffix = " -fx-background-radius: 0; -fx-border-radius: 0;";
        }

        final String baseStyle = baseStylePrefix + radiusSuffix;
        final String selectedStyle = selectedStylePrefix + radiusSuffix;

        btn.setStyle(baseStyle);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle(selectedStyle);
            } else {
                btn.setStyle(baseStyle);
            }
        });

        btn.setOnAction(e -> {
            if (btn.isSelected()) {
                currentViewType = viewType;
                refreshCalendar();
            }
        });

        return btn;
    }

    private void navigatePrevious() {
        switch (currentViewType) {
            case MONTH:
                currentYearMonth = currentYearMonth.minusMonths(1);
                currentDate = currentYearMonth.atDay(1);
                break;
            case WEEK:
                currentDate = currentDate.minusWeeks(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case DAY:
                currentDate = currentDate.minusDays(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
        }
        refreshCalendar();
    }

    private void navigateNext() {
        switch (currentViewType) {
            case MONTH:
                currentYearMonth = currentYearMonth.plusMonths(1);
                currentDate = currentYearMonth.atDay(1);
                break;
            case WEEK:
                currentDate = currentDate.plusWeeks(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case DAY:
                currentDate = currentDate.plusDays(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
        }
        refreshCalendar();
    }

    private String getDateLabelText() {
        switch (currentViewType) {
            case MONTH:
                return currentYearMonth.format(MONTH_YEAR_FORMATTER);
            case WEEK:
                LocalDate weekStart = currentDate.minusDays(currentDate.getDayOfWeek().getValue() % 7);
                LocalDate weekEnd = weekStart.plusDays(6);
                if (weekStart.getMonth() == weekEnd.getMonth()) {
                    return weekStart.format(WEEK_FORMATTER) + " - " + weekEnd.getDayOfMonth() + ", "
                            + weekStart.getYear();
                } else {
                    return weekStart.format(WEEK_FORMATTER) + " - " + weekEnd.format(WEEK_FORMATTER) + ", "
                            + weekStart.getYear();
                }
            case DAY:
                return currentDate.format(DATE_FORMATTER);
            default:
                return "";
        }
    }

    private GridPane createMonthGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-background-color: #FFFFFF;");

        // Create day headers
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int col = 0; col < 7; col++) {
            Label dayHeader = new Label(dayNames[col]);
            dayHeader.setFont(Font.font("Poppins Medium", 13));
            dayHeader.setStyle(
                    "-fx-background-color: #F5F5F5; " +
                            "-fx-text-fill: #616161; " +
                            "-fx-alignment: center;");
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            dayHeader.setPrefHeight(40);
            GridPane.setHgrow(dayHeader, Priority.ALWAYS);
            grid.add(dayHeader, col, 0);
        }

        // Calculate calendar grid
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        LocalDate startDate = firstOfMonth.minusDays(dayOfWeek);

        // Create day cells (6 weeks)
        int row = 1;
        LocalDate currentDate = startDate;

        for (int week = 0; week < 6; week++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = createMonthDayCell(currentDate);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                grid.add(dayCell, col, row);
                currentDate = currentDate.plusDays(1);
            }
            row++;
        }

        return grid;
    }

    private GridPane createWeekGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-background-color: #FFFFFF;");

        // Calculate week start (Sunday)
        LocalDate weekStart = currentDate.minusDays(currentDate.getDayOfWeek().getValue() % 7);

        // Time column
        VBox timeColumn = new VBox(0);
        timeColumn.setMinWidth(60);
        timeColumn.setMaxWidth(60);
        timeColumn.setStyle("-fx-background-color: #F5F5F5;");

        // Empty corner cell
        Label cornerCell = new Label("");
        cornerCell.setPrefHeight(40);
        cornerCell.setStyle("-fx-background-color: #F5F5F5;");
        timeColumn.getChildren().add(cornerCell);

        // Time labels (6 AM to 11 PM)
        for (int hour = 6; hour <= 23; hour++) {
            Label timeLabel = new Label(
                    String.format("%d:00", hour > 12 ? hour - 12 : hour) + (hour >= 12 ? " PM" : " AM"));
            timeLabel.setFont(Font.font("Poppins Regular", 11));
            timeLabel.setPrefHeight(60);
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setPadding(new Insets(5, 5, 0, 0));
            timeLabel.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #616161;");
            timeColumn.getChildren().add(timeLabel);
        }

        grid.add(timeColumn, 0, 0);
        GridPane.setRowSpan(timeColumn, 2);

        // Day headers
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int col = 0; col < 7; col++) {
            LocalDate date = weekStart.plusDays(col);
            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPadding(new Insets(5));
            dayHeader.setStyle("-fx-background-color: #F5F5F5;");

            Label dayName = new Label(dayNames[col]);
            dayName.setFont(Font.font("Poppins Medium", 12));
            dayName.setStyle("-fx-text-fill: #616161;");

            Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
            dayNum.setFont(Font.font("Poppins Bold", 16));

            boolean isToday = date.equals(LocalDate.now());
            if (isToday) {
                dayNum.setStyle(
                        "-fx-text-fill: #FFFFFF; -fx-background-color: #4285f4; -fx-background-radius: 50%; -fx-min-width: 30; -fx-min-height: 30; -fx-alignment: center;");
            } else {
                dayNum.setStyle("-fx-text-fill: #000000;");
            }

            dayHeader.getChildren().addAll(dayName, dayNum);
            GridPane.setHgrow(dayHeader, Priority.ALWAYS);
            grid.add(dayHeader, col + 1, 0);
        }

        // Day columns with time slots
        for (int col = 0; col < 7; col++) {
            LocalDate date = weekStart.plusDays(col);
            VBox dayColumn = createWeekDayColumn(date);
            GridPane.setHgrow(dayColumn, Priority.ALWAYS);
            GridPane.setVgrow(dayColumn, Priority.ALWAYS);
            grid.add(dayColumn, col + 1, 1);
        }

        return grid;
    }

    private VBox createWeekDayColumn(LocalDate date) {
        VBox column = new VBox(0);
        column.setStyle("-fx-background-color: #FFFFFF;");

        // Create time slots (6 AM to 11 PM)
        for (int hour = 6; hour <= 23; hour++) {
            StackPane timeSlot = new StackPane();
            timeSlot.setPrefHeight(60);
            timeSlot.setStyle("-fx-background-color: #FFFFFF;");

            // Get events for this hour
            VBox eventsBox = new VBox(2);
            eventsBox.setPadding(new Insets(2));

            List<CalendarEvent> dayEvents = controller.getEventsForDate(date);
            int finalHour = hour;
            List<CalendarEvent> hourEvents = dayEvents.stream()
                    .filter(e -> !e.getIsAllDay() && e.getStartDateTime().getHour() == finalHour)
                    .collect(Collectors.toList());

            for (CalendarEvent event : hourEvents) {
                Label eventLabel = createWeekEventLabel(event);
                eventsBox.getChildren().add(eventLabel);
            }

            timeSlot.getChildren().add(eventsBox);
            StackPane.setAlignment(eventsBox, Pos.TOP_LEFT);

            // Click handler
            timeSlot.setOnMouseClicked(e -> {
                controller.createEvent(date);
            });

            timeSlot.setOnMouseEntered(e -> timeSlot.setStyle(
                    "-fx-border-color: #F0F0F0; -fx-border-width: 0.5 0 0 0; -fx-background-color: #F9F9F9; -fx-cursor: hand;"));
            timeSlot.setOnMouseExited(e -> timeSlot
                    .setStyle(
                            "-fx-border-color: #F0F0F0; -fx-border-width: 0.5 0 0 0; -fx-background-color: #FFFFFF;"));

            column.getChildren().add(timeSlot);
        }

        return column;
    }

    private Label createWeekEventLabel(CalendarEvent event) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        String timeStr = event.getStartDateTime().toLocalTime().toString();
        Label label = new Label(timeStr + " " + event.getTitle());
        label.setFont(Font.font("Poppins Regular", 11));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-padding: 3 5 3 5; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand;");

        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createDayView() {
        VBox dayView = new VBox(0);
        dayView.setStyle("-fx-background-color: #FFFFFF;");

        // All-day events section
        VBox allDaySection = new VBox(5);
        allDaySection.setPadding(new Insets(10));
        allDaySection.setStyle("-fx-background-color: #F5F5F5;");

        Label allDayLabel = new Label("All Day");
        allDayLabel.setFont(Font.font("Poppins Medium", 12));
        allDayLabel.setStyle("-fx-text-fill: #616161;");
        allDaySection.getChildren().add(allDayLabel);

        List<CalendarEvent> dayEvents = controller.getEventsForDate(currentDate);
        List<CalendarEvent> allDayEvents = dayEvents.stream()
                .filter(CalendarEvent::getIsAllDay)
                .collect(Collectors.toList());

        if (!allDayEvents.isEmpty()) {
            for (CalendarEvent event : allDayEvents) {
                Label eventLabel = createEventLabel(event);
                allDaySection.getChildren().add(eventLabel);
            }
        } else {
            Label noEvents = new Label("No all-day events");
            noEvents.setFont(Font.font("Poppins Regular", 11));
            noEvents.setStyle("-fx-text-fill: #9E9E9E;");
            allDaySection.getChildren().add(noEvents);
        }

        dayView.getChildren().add(allDaySection);

        // Time slots
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        HBox timeSlotContainer = new HBox(0);

        // Time column
        VBox timeColumn = new VBox(0);
        timeColumn.setMinWidth(80);
        timeColumn.setMaxWidth(80);
        timeColumn.setStyle("-fx-background-color: #F5F5F5;");

        // Event column
        VBox eventColumn = new VBox(0);
        HBox.setHgrow(eventColumn, Priority.ALWAYS);

        // Create time slots (24 hours)
        for (int hour = 0; hour < 24; hour++) {
            // Time label
            Label timeLabel = new Label(String.format("%d:00", hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour))
                    + (hour >= 12 ? " PM" : " AM"));
            timeLabel.setFont(Font.font("Poppins Regular", 12));
            timeLabel.setPrefHeight(80);
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setPadding(new Insets(5, 10, 0, 0));
            timeLabel.setStyle(
                    "-fx-background-color: #F5F5F5; -fx-text-fill: #616161;");
            timeColumn.getChildren().add(timeLabel);

            // Time slot
            StackPane timeSlot = new StackPane();
            timeSlot.setPrefHeight(80);
            timeSlot.setStyle("-fx-background-color: #FFFFFF;");

            // Get events for this hour
            VBox eventsBox = new VBox(3);
            eventsBox.setPadding(new Insets(5));

            int finalHour = hour;
            List<CalendarEvent> hourEvents = dayEvents.stream()
                    .filter(e -> !e.getIsAllDay() && e.getStartDateTime().getHour() == finalHour)
                    .collect(Collectors.toList());

            for (CalendarEvent event : hourEvents) {
                Label eventLabel = createDayEventLabel(event);
                eventsBox.getChildren().add(eventLabel);
            }

            timeSlot.getChildren().add(eventsBox);
            StackPane.setAlignment(eventsBox, Pos.TOP_LEFT);

            // Click handler
            timeSlot.setOnMouseClicked(e -> {
                controller.createEvent(currentDate);
            });

            timeSlot.setOnMouseEntered(e -> timeSlot.setStyle(
                    "-fx-background-color: #F9F9F9; -fx-cursor: hand;"));
            timeSlot.setOnMouseExited(e -> timeSlot
                    .setStyle(
                            "-fx-background-color: #FFFFFF;"));

            eventColumn.getChildren().add(timeSlot);
        }

        timeSlotContainer.getChildren().addAll(timeColumn, eventColumn);
        scrollPane.setContent(timeSlotContainer);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        dayView.getChildren().add(scrollPane);

        return dayView;
    }

    private Label createDayEventLabel(CalendarEvent event) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        String timeStr = event.getStartDateTime().toLocalTime().toString() + " - "
                + event.getEndDateTime().toLocalTime().toString();
        String text = event.getTitle() + "\n" + timeStr;
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            text += "\n📍 " + event.getLocation();
        }

        Label label = new Label(text);
        label.setFont(Font.font("Poppins Regular", 12));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-padding: 8 10 8 10; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;");

        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createMonthDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(5));
        cell.setMinHeight(100);
        cell.setStyle("-fx-background-color: #FFFFFF;");

        // Check if today
        boolean isToday = date.equals(LocalDate.now());
        // Check if current month
        boolean isCurrentMonth = YearMonth.from(date).equals(currentYearMonth);

        if (isToday) {
            cell.setStyle("-fx-background-color: #E3F2FD;");
        }

        // Day number
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins Regular", 14));
        dayLabel.setStyle("-fx-text-fill: " + (isCurrentMonth ? "#000000" : "#BDBDBD"));
        dayLabel.setAlignment(Pos.TOP_RIGHT);
        dayLabel.setMaxWidth(Double.MAX_VALUE);

        cell.getChildren().add(dayLabel);

        // Get events for this date
        List<CalendarEvent> eventsOnDate = controller.getEventsForDate(date);

        // Show up to 3 events
        int count = 0;
        for (CalendarEvent event : eventsOnDate) {
            if (count >= 3) {
                int remaining = eventsOnDate.size() - 3;
                Label moreLabel = new Label("+" + remaining + " more");
                moreLabel.setFont(Font.font("Poppins Regular", 11));
                moreLabel.setStyle(
                        "-fx-text-fill: #4285f4; " +
                                "-fx-cursor: hand; " +
                                "-fx-underline: true;");
                cell.getChildren().add(moreLabel);
                break;
            }

            Label eventLabel = createEventLabel(event);
            cell.getChildren().add(eventLabel);
            count++;
        }

        // Click handler
        final LocalDate cellDate = date;
        cell.setOnMouseClicked(e -> controller.createEvent(cellDate));

        // Hover effect
        cell.setOnMouseEntered(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: #F5F5F5; -fx-cursor: hand;");
            }
        });
        cell.setOnMouseExited(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: #FFFFFF;");
            }
        });

        return cell;
    }

    private Label createEventLabel(CalendarEvent event) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        Label label = new Label(event.getTitle());
        label.setFont(Font.font("Poppins Regular", 11));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-padding: 2 5 2 5; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand;");

        // Truncate text
        label.setMaxHeight(18);

        // Click to edit
        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createFilterPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(280);
        panel.setMinWidth(280);
        panel.setMaxWidth(280);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        // Calendars section
        Label calendarsLabel = new Label("Calendars");
        calendarsLabel.setFont(Font.font("Poppins Bold", 16));

        VBox calendarsBox = new VBox(8);
        for (CalendarSource source : controller.getCalendarSources()) {
            CheckBox cb = new CheckBox(source.getName());
            cb.setFont(Font.font("Poppins Regular", 14));
            cb.setSelected(source.getIsVisible());

            // Color indicator
            javafx.scene.shape.Circle colorDot = new javafx.scene.shape.Circle(5);
            colorDot.setFill(javafx.scene.paint.Color.web(source.getColor()));
            cb.setGraphic(colorDot);

            cb.setOnAction(e -> {
                controller.toggleCalendarVisibility(source.getId(), cb.isSelected());
                refreshCalendar();
            });

            calendarsBox.getChildren().add(cb);
        }

        panel.getChildren().addAll(calendarsLabel, calendarsBox);

        return panel;
    }

    private void toggleFilterPanel() {
        if (getRight() == null) {
            setRight(filterPanel);
        } else {
            setRight(null);
        }
    }

    private void refreshCalendar() {
        dateLabel.setText(getDateLabelText());

        // Recreate calendar view based on current view type
        calendarContainer.getChildren().clear();

        switch (currentViewType) {
            case MONTH:
                calendarContainer.getChildren().add(createMonthGrid());
                break;
            case WEEK:
                ScrollPane weekScroll = new ScrollPane(createWeekGrid());
                weekScroll.setFitToWidth(true);
                weekScroll.setStyle("-fx-background-color: transparent;");
                calendarContainer.getChildren().add(weekScroll);
                break;
            case DAY:
                calendarContainer.getChildren().add(createDayView());
                break;
        }
    }
}
