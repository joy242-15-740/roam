package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.controller.JournalController;
import com.roam.controller.OperationsController;
import com.roam.controller.WikiController;
import com.roam.model.Operation;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        contentPane.setSpacing(30);
        // Remove manual background
        // contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Header
        Label headerLabel = new Label("Statistics");
        headerLabel.getStyleClass().add(atlantafx.base.theme.Styles.TITLE_1);
        contentPane.getChildren().add(headerLabel);

        // Overall stats
        GridPane grid = new GridPane();
        grid.setHgap(50);
        grid.setVgap(30);

        // Data fetching
        List<Wiki> allNotes = wikiController.loadAllNotes();
        long totalWikis = allNotes.size();
        long totalJournals = journalController.loadAllEntries().size();
        long totalEvents = calendarController.getAllEvents().size();
        long totalOperations = operationsController.loadOperations().size();

        addStat(grid, 0, 0, "Total Operations", formatCount(totalOperations));
        addStat(grid, 1, 0, "Total Wikis", formatCount(totalWikis));
        addStat(grid, 0, 1, "Total Journals", formatCount(totalJournals));
        addStat(grid, 1, 1, "Total Events", formatCount(totalEvents));

        contentPane.getChildren().add(grid);

        // Charts Container
        HBox chartsBox = new HBox(30);
        chartsBox.setAlignment(Pos.CENTER_LEFT);

        // Task Status Chart
        VBox taskChartBox = createTaskStatusChart();

        // Operations Chart (Tasks by Operation)
        VBox opsChartBox = createOperationsChart();

        chartsBox.getChildren().addAll(taskChartBox, opsChartBox);
        contentPane.getChildren().add(chartsBox);
    }

    private String formatCount(long count) {
        if (count > 1000) {
            return String.format("%.1fK+", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private VBox createTaskStatusChart() {
        VBox container = new VBox(10);
        container.getStyleClass().add(atlantafx.base.theme.Styles.ELEVATED_1);
        container.setPadding(new Insets(20));

        Label header = new Label("Task Status");
        header.getStyleClass().add(atlantafx.base.theme.Styles.TITLE_4);

        long doneTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("To Do", todoTasks),
                new PieChart.Data("In Progress", inProgressTasks),
                new PieChart.Data("Done", doneTasks));

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setPrefHeight(300);
        chart.setMaxHeight(300);

        container.getChildren().addAll(header, chart);
        return container;
    }

    private VBox createOperationsChart() {
        VBox container = new VBox(10);
        container.getStyleClass().add(atlantafx.base.theme.Styles.ELEVATED_1);
        container.setPadding(new Insets(20));

        Label header = new Label("Tasks by Operation");
        header.getStyleClass().add(atlantafx.base.theme.Styles.TITLE_4);

        List<Task> allTasks = taskRepository.findAll();
        List<Operation> allOps = operationsController.loadOperations();

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
        chart.setPrefHeight(300);
        chart.setMaxHeight(300);

        container.getChildren().addAll(header, chart);
        return container;
    }

    private void addStat(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add(atlantafx.base.theme.Styles.ELEVATED_1);
        box.setPadding(new Insets(20));
        box.setPrefWidth(250);

        Label valLabel = new Label(value);
        valLabel.getStyleClass().addAll(atlantafx.base.theme.Styles.TITLE_1, atlantafx.base.theme.Styles.ACCENT);

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_MUTED);

        box.getChildren().addAll(valLabel, textLabel);
        grid.add(box, col, row);
    }
}
