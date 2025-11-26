package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.controller.JournalController;
import com.roam.controller.OperationsController;
import com.roam.controller.WikiController;
import com.roam.model.JournalEntry;
import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Wiki;
import com.roam.repository.TaskRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.roam.util.UIConstants.*;

public class StatisticsView extends StackPane {

    private final WikiController wikiController;
    private final JournalController journalController;
    private final CalendarController calendarController;
    private final OperationsController operationsController;
    private final TaskRepository taskRepository;
    private final VBox contentPane;

    public StatisticsView(WikiController wikiController) {
        this.wikiController = wikiController;
        this.journalController = new JournalController();
        this.calendarController = new CalendarController();
        this.operationsController = new OperationsController();
        this.taskRepository = new TaskRepository();

        this.contentPane = new VBox();

        ScrollPane scrollPane = new ScrollPane(contentPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        getChildren().add(scrollPane);
        initializeView();
    }

    private void initializeView() {
        contentPane.setPadding(new Insets(30));
        contentPane.setSpacing(24);

        // Header
        Label headerLabel = new Label("Statistics Dashboard");
        headerLabel.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 24px;");
        contentPane.getChildren().add(headerLabel);

        // Fetch all data once
        List<Wiki> allNotes = wikiController.loadAllNotes();
        List<JournalEntry> allJournals = journalController.loadAllEntries();
        List<Operation> allOps = operationsController.loadOperations();
        List<Task> allTasks = taskRepository.findAll();

        long totalWikis = allNotes.size();
        long totalJournals = allJournals.size();
        long totalEvents = calendarController.getAllEvents().size();
        long totalOperations = allOps.size();
        long totalTasks = allTasks.size();

        long doneTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long overdueTasks = taskRepository.countOverdue();
        long highPriorityTasks = taskRepository.countHighPriority();

        // Calculate completion rate
        double completionRate = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;

        // Count operations by status
        long ongoingOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.ONGOING).count();
        long inProgressOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.IN_PROGRESS).count();
        long endedOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.END).count();

        // ========== OVERVIEW SECTION ==========
        Label overviewLabel = new Label("Overview");
        overviewLabel.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px;");
        contentPane.getChildren().add(overviewLabel);

        // First row of stat cards - 5 main counts
        FlowPane overviewGrid = new FlowPane();
        overviewGrid.setHgap(16);
        overviewGrid.setVgap(16);

        overviewGrid.getChildren().addAll(
                createStatCard("Operations", formatCount(totalOperations), Feather.GIT_BRANCH, "#4285f4"),
                createStatCard("Tasks", formatCount(totalTasks), Feather.CHECK_SQUARE, "#388E3C"),
                createStatCard("Wikis", formatCount(totalWikis), Feather.FILE_TEXT, "#9c27b0"),
                createStatCard("Journals", formatCount(totalJournals), Feather.BOOK, "#F57C00"),
                createStatCard("Events", formatCount(totalEvents), Feather.CALENDAR, "#D32F2F"));
        contentPane.getChildren().add(overviewGrid);

        // ========== TASKS SECTION ==========
        Label tasksLabel = new Label("Task Metrics");
        tasksLabel.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px; -fx-padding: 10 0 0 0;");
        contentPane.getChildren().add(tasksLabel);

        // Task metrics row
        FlowPane taskMetricsGrid = new FlowPane();
        taskMetricsGrid.setHgap(16);
        taskMetricsGrid.setVgap(16);

        taskMetricsGrid.getChildren().addAll(
                createCompletionCard(completionRate, doneTasks, totalTasks),
                createStatCard("To Do", formatCount(todoTasks), Feather.CIRCLE, "#F57C00"),
                createStatCard("In Progress", formatCount(inProgressTasks), Feather.LOADER, "#1976D2"),
                createStatCard("Completed", formatCount(doneTasks), Feather.CHECK_CIRCLE, "#388E3C"),
                createAlertCard("Overdue", formatCount(overdueTasks), Feather.ALERT_TRIANGLE, "#D32F2F"),
                createAlertCard("High Priority", formatCount(highPriorityTasks), Feather.FLAG, "#C62828"));
        contentPane.getChildren().add(taskMetricsGrid);

        // ========== CHARTS SECTION ==========
        Label chartsLabel = new Label("Visual Breakdown");
        chartsLabel.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px; -fx-padding: 10 0 0 0;");
        contentPane.getChildren().add(chartsLabel);

        // Charts Container - horizontal layout
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.TOP_LEFT);

        // Task Status Chart
        VBox taskChartBox = createTaskStatusChart(todoTasks, inProgressTasks, doneTasks);

        // Tasks by Operation Chart
        VBox opsChartBox = createOperationsChart(allTasks, allOps);

        // Tasks by Priority Chart
        VBox priorityChartBox = createPriorityChart(allTasks);

        chartsBox.getChildren().addAll(taskChartBox, opsChartBox, priorityChartBox);
        contentPane.getChildren().add(chartsBox);

        // ========== OPERATIONS SECTION ==========
        Label opsLabel = new Label("Operation Status");
        opsLabel.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 18px; -fx-padding: 10 0 0 0;");
        contentPane.getChildren().add(opsLabel);

        FlowPane opsGrid = new FlowPane();
        opsGrid.setHgap(16);
        opsGrid.setVgap(16);

        opsGrid.getChildren().addAll(
                createStatCard("Ongoing", formatCount(ongoingOps), Feather.PLAY, "#4285f4"),
                createStatCard("In Progress", formatCount(inProgressOps), Feather.CLOCK, "#F57C00"),
                createStatCard("Completed", formatCount(endedOps), Feather.CHECK_CIRCLE, "#388E3C"));
        contentPane.getChildren().add(opsGrid);
    }

    /**
     * Creates a stat card with icon, value, and label
     */
    private VBox createStatCard(String label, String value, org.kordamp.ikonli.Ikon icon, String colorHex) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setMinWidth(160);
        card.setStyle(
                "-fx-background-color: -roam-bg-primary;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: -roam-border;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        // Icon in colored circle
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web(colorHex, 0.15));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.web(colorHex));

        iconContainer.getChildren().addAll(iconBg, fontIcon);

        // Value
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 28px;");

        // Label
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: -roam-text-secondary;");

        card.getChildren().addAll(iconContainer, valLabel, textLabel);
        return card;
    }

    /**
     * Creates an alert-style card for warning metrics (overdue, high priority)
     */
    private VBox createAlertCard(String label, String value, org.kordamp.ikonli.Ikon icon, String colorHex) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setMinWidth(160);

        // Use a subtle colored background for alerts
        card.setStyle(
                "-fx-background-color: " + colorHex + "15;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + colorHex + "40;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        // Icon in colored circle
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web(colorHex, 0.25));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.web(colorHex));

        iconContainer.getChildren().addAll(iconBg, fontIcon);

        // Value
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 28px; -fx-text-fill: " + colorHex + ";");

        // Label
        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-font-family: 'Poppins Medium'; -fx-font-size: 13px; -fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(iconContainer, valLabel, textLabel);
        return card;
    }

    /**
     * Creates a completion rate card with progress bar
     */
    private VBox createCompletionCard(double completionRate, long completed, long total) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setMinWidth(200);
        card.setStyle(
                "-fx-background-color: -roam-bg-primary;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: -roam-border;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        // Header with icon
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(16);
        iconBg.setFill(Color.web("#388E3C", 0.15));

        FontIcon icon = new FontIcon(Feather.TRENDING_UP);
        icon.setIconSize(16);
        icon.setIconColor(Color.web("#388E3C"));

        iconContainer.getChildren().addAll(iconBg, icon);

        Label titleLabel = new Label("Completion Rate");
        titleLabel.setStyle(
                "-fx-font-family: 'Poppins Medium'; -fx-font-size: 13px; -fx-text-fill: -roam-text-secondary;");

        header.getChildren().addAll(iconContainer, titleLabel);

        // Percentage value
        Label valLabel = new Label(String.format("%.0f%%", completionRate));
        valLabel.setStyle("-fx-font-family: 'Poppins Bold'; -fx-font-size: 32px; -fx-text-fill: #388E3C;");

        // Progress bar
        ProgressBar progressBar = new ProgressBar(completionRate / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setStyle(
                "-fx-accent: #388E3C;" +
                        "-fx-background-radius: 4;" +
                        "-fx-control-inner-background: -roam-gray-bg;");

        // Completed count
        Label countLabel = new Label(completed + " of " + total + " tasks");
        countLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: -roam-text-hint;");

        card.getChildren().addAll(header, valLabel, progressBar, countLabel);
        return card;
    }

    private String formatCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private VBox createTaskStatusChart(long todo, long inProgress, long done) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: -roam-bg-primary;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: -roam-border;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        Label header = new Label("Task Status");
        header.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 16px;");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("To Do", todo),
                new PieChart.Data("In Progress", inProgress),
                new PieChart.Data("Done", done));

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);

        // Style the chart
        chart.setStyle("-fx-font-family: 'Poppins';");

        container.getChildren().addAll(header, chart);
        return container;
    }

    private VBox createOperationsChart(List<Task> allTasks, List<Operation> allOps) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: -roam-bg-primary;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: -roam-border;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        Label header = new Label("Tasks by Operation");
        header.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 16px;");

        Map<Long, String> opNames = allOps.stream()
                .collect(Collectors.toMap(Operation::getId, Operation::getName));

        Map<String, Long> tasksByOp = allTasks.stream()
                .filter(t -> t.getOperationId() != null)
                .collect(Collectors.groupingBy(
                        t -> opNames.getOrDefault(t.getOperationId(), "Unknown"),
                        Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        tasksByOp.forEach((name, count) -> pieChartData.add(new PieChart.Data(name, count)));

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);
        chart.setStyle("-fx-font-family: 'Poppins';");

        container.getChildren().addAll(header, chart);
        return container;
    }

    private VBox createPriorityChart(List<Task> allTasks) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.setStyle(
                "-fx-background-color: -roam-bg-primary;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: -roam-border;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");

        Label header = new Label("Tasks by Priority");
        header.setStyle("-fx-font-family: 'Poppins SemiBold'; -fx-font-size: 16px;");

        // Count tasks by priority
        long highCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.HIGH).count();
        long mediumCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.MEDIUM).count();
        long lowCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.LOW).count();
        long noPriorityCount = allTasks.stream()
                .filter(t -> t.getPriority() == null).count();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if (highCount > 0)
            pieChartData.add(new PieChart.Data("High", highCount));
        if (mediumCount > 0)
            pieChartData.add(new PieChart.Data("Medium", mediumCount));
        if (lowCount > 0)
            pieChartData.add(new PieChart.Data("Low", lowCount));
        if (noPriorityCount > 0)
            pieChartData.add(new PieChart.Data("None", noPriorityCount));

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);
        chart.setStyle("-fx-font-family: 'Poppins';");

        container.getChildren().addAll(header, chart);
        return container;
    }
}
