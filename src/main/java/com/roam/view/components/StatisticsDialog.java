package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

public class StatisticsDialog extends Dialog<Void> {

    public StatisticsDialog(WikiController controller) {
        setTitle("Wiki Statistics");
        setHeaderText("Your Wiki/Notes Overview");
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        // Overall stats
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(20);

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

        mainContent.getChildren().add(grid);

        getDialogPane().setContent(mainContent);
        getDialogPane().setPrefWidth(400);
    }

    private void addStat(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(javafx.geometry.Pos.CENTER);

        Label valLabel = new Label(value);
        valLabel.setFont(Font.font("Poppins Bold", 28));
        valLabel.setStyle("-fx-text-fill: -roam-blue;");

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("Poppins Regular", 13));
        textLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        box.getChildren().addAll(valLabel, textLabel);
        grid.add(box, col, row);
    }
}
