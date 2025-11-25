package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import com.roam.model.TaskStatus;
import com.roam.repository.TaskRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

public class StatisticsView extends StackPane {

    private final WikiController controller;
    private final TaskRepository taskRepository;
    private final VBox contentPane;

    public StatisticsView(WikiController controller) {
        this.controller = controller;
        this.taskRepository = new TaskRepository();
        this.contentPane = new VBox();
        getChildren().add(contentPane);
        initializeView();

        // Add listeners for responsive scaling
        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleContent());
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleContent());
    }

    private void scaleContent() {
        double width = getWidth();
        double height = getHeight();

        // Use layout bounds to get the actual size of the content
        double contentWidth = contentPane.getLayoutBounds().getWidth();
        double contentHeight = contentPane.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        // Calculate scale factors
        double scaleX = width < contentWidth ? width / contentWidth : 1.0;
        double scaleY = height < contentHeight ? height / contentHeight : 1.0;

        // Use the smaller scale to maintain aspect ratio and fit within bounds
        double scale = Math.min(scaleX, scaleY);

        contentPane.setScaleX(scale);
        contentPane.setScaleY(scale);
    }

    private void initializeView() {
        contentPane.setPadding(new Insets(30));
        contentPane.setSpacing(30);
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Header
        Label headerLabel = new Label("Statistics");
        headerLabel.setFont(Font.font("Poppins Bold", 28));
        headerLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        contentPane.getChildren().add(headerLabel);

        // Overall stats
        GridPane grid = new GridPane();
        grid.setHgap(50);
        grid.setVgap(30);

        List<Wiki> allNotes = controller.loadAllNotes();

        long totalNotes = allNotes.size();
        long totalWords = allNotes.stream()
                .mapToLong(n -> n.getWordCount() != null ? n.getWordCount() : 0)
                .sum();
        long favorites = allNotes.stream().filter(n -> n.getIsFavorite() != null && n.getIsFavorite()).count();
        long avgWords = totalNotes > 0 ? totalWords / totalNotes : 0;

        addStat(grid, 0, 0, "Total Wikis", String.valueOf(totalNotes));
        addStat(grid, 1, 0, "Total Words", String.format("%,d", totalWords));
        addStat(grid, 0, 1, "Favorites", String.valueOf(favorites));
        addStat(grid, 1, 1, "Avg Word Count", String.valueOf(avgWords));

        contentPane.getChildren().add(grid);

        // Task Statistics
        Label taskHeader = new Label("Task Status");
        taskHeader.setFont(Font.font("Poppins Bold", 20));
        taskHeader.setStyle("-fx-text-fill: -roam-text-primary;");

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
        // Make chart legend text color compatible with dark mode
        chart.setStyle("-fx-text-fill: -roam-text-primary;");

        contentPane.getChildren().addAll(taskHeader, chart);
    }

    private void addStat(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("Poppins Bold", 36));
        valLabel.setStyle("-fx-text-fill: -roam-blue;");

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("Poppins Regular", 14));
        textLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        box.getChildren().addAll(valLabel, textLabel);
        grid.add(box, col, row);
    }
}
