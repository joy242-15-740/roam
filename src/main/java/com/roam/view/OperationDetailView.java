package com.roam.view;

import com.roam.controller.OperationDetailController;
import com.roam.model.Note;
import com.roam.model.Operation;
import com.roam.view.components.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class OperationDetailView extends BorderPane {

    private final OperationDetailController controller;
    private final Runnable onNavigateBack;

    private OperationInfoCard infoCard;
    private KanbanBoard kanbanBoard;
    private NotesEditor notesEditor;
    private StackPane tasksCalendarContainer;

    private boolean showingKanban = true;

    public OperationDetailView(Operation operation, Runnable onNavigateBack) {
        this.controller = new OperationDetailController(operation);
        this.onNavigateBack = onNavigateBack;

        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: #FFFFFF;");

        // Set data change listener
        controller.setOnDataChanged(this::refreshData);

        // Create breadcrumb
        Breadcrumb breadcrumb = new Breadcrumb(controller.getOperation().getName(), onNavigateBack);
        setTop(breadcrumb);

        // Create center content
        VBox centerContent = createCenterContent();
        setCenter(centerContent);
    }

    private VBox createCenterContent() {
        VBox content = new VBox(0);
        content.setPadding(new Insets(20));

        // Operation info card
        infoCard = new OperationInfoCard(controller.getOperation(), this::editOperation);

        // TabPane for Tasks and Notes
        TabPane tabPane = createTabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        content.getChildren().addAll(infoCard, tabPane);
        return content;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-family: 'Poppins Regular';");

        // Tab 1: Tasks & Calendar
        Tab tasksTab = new Tab("✓📅 Tasks & Calendar");
        tasksTab.setContent(createTasksCalendarView());

        // Tab 2: Notes
        Tab notesTab = new Tab("📝 Notes");
        notesTab.setContent(createNotesView());

        tabPane.getTabs().addAll(tasksTab, notesTab);

        // Apply custom tab styling
        tabPane.getStyleClass().add("custom-tab-pane");

        return tabPane;
    }

    private BorderPane createTasksCalendarView() {
        BorderPane view = new BorderPane();

        // Toggle buttons
        HBox toggleBar = createToggleBar();
        view.setTop(toggleBar);

        // Content container
        tasksCalendarContainer = new StackPane();
        view.setCenter(tasksCalendarContainer);

        // Create kanban board
        kanbanBoard = new KanbanBoard();
        kanbanBoard.setOnAddTask(controller::createTask);
        kanbanBoard.setOnEditTask(controller::editTask);
        kanbanBoard.setOnTaskStatusChanged(controller::updateTaskStatus);

        // Show kanban by default
        showKanban();

        return view;
    }

    private HBox createToggleBar() {
        HBox bar = new HBox(0);
        bar.setPadding(new Insets(15));
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #F5F5F5;");

        ToggleGroup group = new ToggleGroup();

        ToggleButton kanbanBtn = createToggleButton("📋 Kanban", true);
        ToggleButton calendarBtn = createToggleButton("📅 Calendar", false);

        kanbanBtn.setToggleGroup(group);
        calendarBtn.setToggleGroup(group);

        kanbanBtn.setSelected(true);

        kanbanBtn.setOnAction(e -> showKanban());
        calendarBtn.setOnAction(e -> showCalendar());

        // Style toggle buttons
        String baseStyle = "-fx-padding: 10 20 10 20; " +
                "-fx-font-family: 'Poppins Regular'; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: #E0E0E0; " +
                "-fx-border-width: 1;";

        kanbanBtn.setStyle(baseStyle + "-fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;");
        calendarBtn.setStyle(baseStyle + "-fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;");

        updateToggleButtonStyle(kanbanBtn, true);
        updateToggleButtonStyle(calendarBtn, false);

        kanbanBtn.selectedProperty()
                .addListener((obs, old, selected) -> updateToggleButtonStyle(kanbanBtn, selected));
        calendarBtn.selectedProperty()
                .addListener((obs, old, selected) -> updateToggleButtonStyle(calendarBtn, selected));

        HBox buttonContainer = new HBox(0, kanbanBtn, calendarBtn);
        bar.getChildren().add(buttonContainer);

        return bar;
    }

    private ToggleButton createToggleButton(String text, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setPrefWidth(120);
        btn.setPrefHeight(40);
        return btn;
    }

    private void updateToggleButtonStyle(ToggleButton button, boolean selected) {
        String baseStyle = "-fx-padding: 10 20 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: " + (selected ? "#4285f4" : "#E0E0E0") + "; " +
                "-fx-border-width: 1; " +
                "-fx-background-color: " + (selected ? "#FFFFFF" : "transparent") + "; " +
                "-fx-text-fill: " + (selected ? "#4285f4" : "#616161") + "; " +
                "-fx-font-family: 'Poppins " + (selected ? "Medium" : "Regular") + "'; " +
                "-fx-font-size: 14px;";

        if (button.getText().contains("Kanban")) {
            button.setStyle(baseStyle + "-fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;");
        } else {
            button.setStyle(baseStyle + "-fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;");
        }
    }

    private void showKanban() {
        showingKanban = true;
        kanbanBoard.loadTasks(controller.loadTasks());
        tasksCalendarContainer.getChildren().clear();
        tasksCalendarContainer.getChildren().add(kanbanBoard);
    }

    private void showCalendar() {
        showingKanban = false;
        // Calendar view placeholder
        Label placeholder = new Label("📅 Calendar View\n\nComing in next update");
        placeholder.setFont(Font.font("Poppins Regular", 18));
        placeholder.setStyle("-fx-text-fill: #9E9E9E;");
        tasksCalendarContainer.getChildren().clear();
        tasksCalendarContainer.getChildren().add(placeholder);
    }

    private BorderPane createNotesView() {
        notesEditor = new NotesEditor();
        notesEditor.setOnSave(controller::saveNote);
        notesEditor.setOnDelete(note -> {
            controller.deleteNote(note);
            refreshNotes();
        });
        notesEditor.setOnNewNote(() -> {
            Note newNote = controller.createNote();
            if (newNote != null) {
                refreshNotes();
                notesEditor.selectNote(newNote);
            }
        });
        notesEditor.setOnTitleChanged((note, title) -> {
            // Title will be saved on auto-save
        });

        // Load notes
        notesEditor.loadNotes(controller.loadNotes());

        return notesEditor;
    }

    private void editOperation(Operation operation) {
        OperationDialog dialog = new OperationDialog(operation);
        dialog.showAndWait().ifPresent(updatedOp -> {
            controller.updateOperation(updatedOp);
            infoCard.refresh(updatedOp);
        });
    }

    private void refreshData() {
        // Refresh tasks
        if (showingKanban) {
            kanbanBoard.loadTasks(controller.loadTasks());
        }

        // Refresh notes
        refreshNotes();
    }

    private void refreshNotes() {
        notesEditor.loadNotes(controller.loadNotes());
    }
}
