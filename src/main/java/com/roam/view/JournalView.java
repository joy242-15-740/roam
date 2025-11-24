package com.roam.view;

import com.roam.controller.JournalController;
import com.roam.model.JournalEntry;
import com.roam.model.JournalTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class JournalView extends BorderPane {

    private final JournalController controller;
    private ListView<JournalEntry> entryList;
    private TextArea editorArea;
    private Label dateLabel;
    private JournalEntry currentEntry;

    public JournalView() {
        this.controller = new JournalController();
        initialize();
    }

    private void initialize() {
        // Left Sidebar (List)
        VBox sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");

        Label header = new Label("Journal");
        header.setFont(Font.font("Poppins Bold", 18));

        Button todayBtn = new Button("Today's Entry");
        todayBtn.setMaxWidth(Double.MAX_VALUE);
        todayBtn.setStyle(
                "-fx-background-color: -roam-blue; -fx-text-fill: #FFFFFF; -fx-background-radius: 4; -fx-cursor: hand;");
        todayBtn.setOnAction(e -> openToday());

        entryList = new ListView<>();
        entryList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(JournalEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    setFont(Font.font("Poppins Regular", 14));
                }
            }
        });
        entryList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null)
                loadEntry(newVal);
        });
        VBox.setVgrow(entryList, Priority.ALWAYS);

        sidebar.getChildren().addAll(header, todayBtn, new Separator(), entryList);
        setLeft(sidebar);

        // Center (Editor)
        VBox editorPane = new VBox(15);
        editorPane.setPadding(new Insets(20));
        editorPane.setStyle("-fx-background-color: -roam-gray-bg;");

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        dateLabel = new Label("Select an entry");
        dateLabel.setFont(Font.font("Poppins Bold", 24));

        Button templatesBtn = new Button("Templates");
        templatesBtn.setOnAction(e -> showTemplates());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: -roam-blue; -fx-text-fill: #FFFFFF; -fx-background-radius: 4;");
        saveBtn.setOnAction(e -> saveCurrent());

        toolbar.getChildren().addAll(dateLabel, spacer, templatesBtn, saveBtn);

        editorArea = new TextArea();
        editorArea.setWrapText(true);
        editorArea.setFont(Font.font("Consolas", 14));
        editorArea.setDisable(true);
        VBox.setVgrow(editorArea, Priority.ALWAYS);

        editorPane.getChildren().addAll(toolbar, editorArea);
        setCenter(editorPane);

        refreshList();
    }

    private void refreshList() {
        List<JournalEntry> entries = controller.loadAllEntries();
        entryList.getItems().setAll(entries);
    }

    private void openToday() {
        LocalDate today = LocalDate.now();
        JournalEntry entry = controller.createEntry(today);
        if (entry != null) {
            refreshList();
            entryList.getSelectionModel().select(entry);
        }
    }

    private void loadEntry(JournalEntry entry) {
        this.currentEntry = entry;
        dateLabel.setText(entry.getDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        editorArea.setText(entry.getContent());
        editorArea.setDisable(false);
    }

    private void saveCurrent() {
        if (currentEntry != null) {
            currentEntry.setContent(editorArea.getText());
            controller.saveEntry(currentEntry);
        }
    }

    private void showTemplates() {
        if (currentEntry == null)
            return;

        List<JournalTemplate> templates = controller.loadTemplates();
        ChoiceDialog<JournalTemplate> dialog = new ChoiceDialog<>(null, templates);
        dialog.setTitle("Select Template");
        dialog.setHeaderText("Choose a template to apply");
        dialog.setContentText("Template:");

        // Add default templates if empty
        if (templates.isEmpty()) {
            JournalTemplate daily = new JournalTemplate("Daily Reflection",
                    "## Daily Reflection\n\n* What went well today?\n* What could be improved?\n* Goals for tomorrow:");
            templates.add(daily);
            dialog.getItems().add(daily);
        }

        dialog.showAndWait().ifPresent(template -> {
            controller.applyTemplate(currentEntry, template);
            editorArea.setText(currentEntry.getContent());
        });
    }
}
