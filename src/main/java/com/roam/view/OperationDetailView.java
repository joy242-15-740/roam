package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.controller.OperationDetailController;
import com.roam.controller.WikiController;
import com.roam.model.Operation;
import com.roam.view.components.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class OperationDetailView extends StackPane {

    private final OperationDetailController controller;
    private final CalendarController calendarController;
    private final WikiController wikiController;
    private final Runnable onNavigateBack;
    private final BorderPane contentPane;

    private OperationInfoCard infoCard;
    private KanbanBoard kanbanBoard;
    private CalendarView calendarView;
    private WikiNoteEditor wikiNoteEditor;
    private com.roam.view.components.WikiSidebar wikiSidebar;
    private StackPane tasksCalendarContainer;
    private ScrollPane mainScrollPane;

    private boolean showingKanban = true;

    public OperationDetailView(Operation operation, Runnable onNavigateBack) {
        this.controller = new OperationDetailController(operation);
        this.calendarController = new CalendarController();
        this.wikiController = new WikiController();
        this.onNavigateBack = onNavigateBack;
        this.contentPane = new BorderPane();
        getChildren().add(contentPane);

        // Set up wiki change listener to update sidebar
        wikiController.addOnNoteChangedListener(wiki -> {
            if (wikiSidebar != null) {
                wikiSidebar.refreshOperationWikis(controller.getOperation());
            }
        });

        initialize();

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

    private void initialize() {
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Set data change listener
        controller.setOnDataChanged(this::refreshData);

        // Create breadcrumb
        Breadcrumb breadcrumb = new Breadcrumb(controller.getOperation().getName(), onNavigateBack);
        contentPane.setTop(breadcrumb);

        // Create center content
        VBox centerContent = createCenterContent();

        // Wrap in ScrollPane for better UX
        mainScrollPane = new ScrollPane(centerContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(false);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background-color: -roam-bg-primary; -fx-background: -roam-bg-primary;");

        contentPane.setCenter(mainScrollPane);
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
        tabPane.setStyle("-fx-font-family: 'Poppins';");
        tabPane.setMinHeight(800);

        // Tab 1: Tasks & Calendar
        Tab tasksTab = new Tab("Tasks & Calendar");
        tasksTab.setGraphic(new FontIcon(Feather.CHECK_SQUARE));
        tasksTab.setContent(createTasksCalendarView());

        // Tab 2: Wiki
        Tab notesTab = new Tab("Wiki");
        notesTab.setGraphic(new FontIcon(Feather.FILE_TEXT));
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
        bar.setStyle("-fx-background-color: -roam-gray-bg;");

        ToggleGroup group = new ToggleGroup();

        ToggleButton kanbanBtn = createToggleButton("Kanban", true);
        kanbanBtn.setGraphic(new FontIcon(Feather.CLIPBOARD));
        ToggleButton calendarBtn = createToggleButton("Calendar", false);
        calendarBtn.setGraphic(new FontIcon(Feather.CALENDAR));

        kanbanBtn.setToggleGroup(group);
        calendarBtn.setToggleGroup(group);

        kanbanBtn.setSelected(true);

        kanbanBtn.setOnAction(e -> showKanban());
        calendarBtn.setOnAction(e -> showCalendar());

        // Style toggle buttons
        String baseStyle = "-fx-padding: 10 20 10 20; " +
                "-fx-font-family: 'Poppins'; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: -roam-border; " +
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
                "-fx-border-color: " + (selected ? "-roam-blue" : "-roam-border") + "; " +
                "-fx-border-width: 1; " +
                "-fx-background-color: " + (selected ? "-roam-bg-primary" : "transparent") + "; " +
                "-fx-text-fill: " + (selected ? "-roam-blue" : "-roam-text-secondary") + "; " +
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

        // Create or update calendar view
        if (calendarView == null) {
            calendarView = new CalendarView(calendarController);
        }

        tasksCalendarContainer.getChildren().clear();
        tasksCalendarContainer.getChildren().add(calendarView);
    }

    private BorderPane createNotesView() {
        BorderPane notesContainer = new BorderPane();

        // Create toolbar for notes
        HBox notesToolbar = createNotesToolbar();
        notesContainer.setTop(notesToolbar);

        // Create wiki sidebar
        wikiSidebar = new com.roam.view.components.WikiSidebar(wikiController);
        wikiSidebar.switchToOperationMode(controller.getOperation());
        notesContainer.setLeft(wikiSidebar);

        // Create wiki note editor
        wikiNoteEditor = new WikiNoteEditor(wikiController);
        notesContainer.setCenter(wikiNoteEditor);

        // Load notes for this operation
        refreshNotes();

        return notesContainer;
    }

    private HBox createNotesToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPrefHeight(60);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        // New Wiki button
        Button newNoteBtn = new Button("+ New Wiki");
        newNoteBtn.getStyleClass().add("action-button");
        newNoteBtn.setPrefWidth(130);
        newNoteBtn.setPrefHeight(40);
        newNoteBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");
        newNoteBtn.setOnAction(e -> {
            // Create new wiki and link to this operation
            com.roam.model.Wiki newNote = wikiController.createNewNote();
            newNote.setOperationId(controller.getOperation().getId());
            newNote.setTitle(controller.getOperation().getName() + " Wiki");
            wikiController.saveCurrentNote();
            // Load the newly created note directly
            wikiNoteEditor.loadNote(newNote);
        });

        // Templates Menu
        MenuButton templatesMenu = new MenuButton("Templates");
        templatesMenu.setGraphic(new FontIcon(Feather.FILE_TEXT));
        templatesMenu.getStyleClass().add("button-secondary");
        templatesMenu.setPrefHeight(40);
        templatesMenu.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");

        // Load templates
        refreshTemplatesMenu(templatesMenu);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions Menu
        MenuButton actionsMenu = new MenuButton("Actions");
        actionsMenu.setGraphic(new FontIcon(Feather.SETTINGS));
        actionsMenu.getStyleClass().add("button-secondary");
        actionsMenu.setPrefHeight(40);
        actionsMenu.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;");

        MenuItem viewAllNotesItem = new MenuItem("View All Operation Wikis");
        viewAllNotesItem.setGraphic(new FontIcon(Feather.LIST));
        viewAllNotesItem.setOnAction(e -> {
            // Show list of all wikis for this operation
            showOperationNotesList();
        });

        MenuItem duplicateWikiItem = new MenuItem("Duplicate Wiki");
        duplicateWikiItem.setGraphic(new FontIcon(Feather.COPY));
        duplicateWikiItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleDuplicateWikiFromMenu();
            }
        });

        MenuItem addBannerItem = new MenuItem("Add/Change Banner");
        addBannerItem.setGraphic(new FontIcon(Feather.IMAGE));
        addBannerItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleAddBannerFromMenu();
            }
        });

        MenuItem removeBannerItem = new MenuItem("Remove Banner");
        removeBannerItem.setGraphic(new FontIcon(Feather.X));
        removeBannerItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleRemoveBannerFromMenu();
            }
        });

        MenuItem exportMdItem = new MenuItem("Export as Markdown");
        exportMdItem.setGraphic(new FontIcon(Feather.FILE));
        exportMdItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleExportMarkdownFromMenu();
            }
        });

        MenuItem exportPdfItem = new MenuItem("Export as PDF");
        exportPdfItem.setGraphic(new FontIcon(Feather.DOWNLOAD));
        exportPdfItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleExportPdfFromMenu();
            }
        });

        MenuItem deleteWikiItem = new MenuItem("Delete Wiki");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setStyle("-fx-icon-color: -roam-red;");
        deleteWikiItem.setGraphic(trashIcon);
        deleteWikiItem.setStyle("-fx-text-fill: -roam-red;");
        deleteWikiItem.setOnAction(e -> {
            if (wikiNoteEditor != null) {
                wikiNoteEditor.handleDeleteWikiFromMenu();
            }
        });

        MenuItem refreshItem = new MenuItem("Refresh Wiki");
        refreshItem.setGraphic(new FontIcon(Feather.REFRESH_CW));
        refreshItem.setOnAction(e -> refreshNotes());

        actionsMenu.getItems().addAll(
                viewAllNotesItem,
                new SeparatorMenuItem(),
                duplicateWikiItem,
                addBannerItem,
                removeBannerItem,
                new SeparatorMenuItem(),
                exportMdItem,
                exportPdfItem,
                new SeparatorMenuItem(),
                deleteWikiItem,
                new SeparatorMenuItem(),
                refreshItem);

        toolbar.getChildren().addAll(newNoteBtn, templatesMenu, spacer, actionsMenu);
        return toolbar;
    }

    private void refreshTemplatesMenu(MenuButton templatesMenu) {
        templatesMenu.getItems().clear();

        // Default templates
        List<com.roam.model.WikiTemplate> defaultTemplates = wikiController.loadDefaultTemplates();
        for (com.roam.model.WikiTemplate template : defaultTemplates) {
            MenuItem item = new MenuItem(template.getIcon() + " " + template.getName());
            item.setOnAction(e -> {
                com.roam.model.Wiki newNote = wikiController.createNoteFromTemplate(template);
                newNote.setOperationId(controller.getOperation().getId());
                wikiController.saveCurrentNote();
                // Load the newly created note directly
                wikiNoteEditor.loadNote(newNote);
            });
            templatesMenu.getItems().add(item);
        }

        // Custom templates
        List<com.roam.model.WikiTemplate> customTemplates = wikiController.loadCustomTemplates();
        if (!customTemplates.isEmpty()) {
            if (!defaultTemplates.isEmpty()) {
                templatesMenu.getItems().add(new SeparatorMenuItem());
            }
            for (com.roam.model.WikiTemplate template : customTemplates) {
                MenuItem item = new MenuItem(template.getIcon() + " " + template.getName());
                item.setOnAction(e -> {
                    com.roam.model.Wiki newNote = wikiController.createNoteFromTemplate(template);
                    newNote.setOperationId(controller.getOperation().getId());
                    wikiController.saveCurrentNote();
                    wikiNoteEditor.loadNotesForOperation(controller.getOperation());
                });
                templatesMenu.getItems().add(item);
            }
        }
    }

    private void showOperationNotesList() {
        java.util.List<com.roam.model.Wiki> operationNotes = wikiController
                .loadNotesForOperation(controller.getOperation());

        if (operationNotes.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Wikis");
            alert.setHeaderText("No wikis found");
            alert.setContentText("There are no wikis associated with this operation yet.");
            alert.showAndWait();
            return;
        }

        // Create dialog to show list of wikis
        Dialog<com.roam.model.Wiki> dialog = new Dialog<>();
        dialog.setTitle("Operation Wikis");
        dialog.setHeaderText("Select a wiki to view:");

        ListView<com.roam.model.Wiki> listView = new ListView<>();
        listView.getItems().addAll(operationNotes);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.roam.model.Wiki item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText((item.getIsFavorite() ? "⭐ " : "") + item.getTitle() +
                            "\n   " + item.getWordCount() + " words • " +
                            (item.getUpdatedAt() != null ? item.getUpdatedAt().format(
                                    java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) : ""));
                    setStyle("-fx-font-family: 'Poppins'; -fx-padding: 10;");
                }
            }
        });
        listView.setPrefHeight(300);
        listView.setPrefWidth(400);

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                com.roam.model.Wiki selectedNote = listView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    dialog.setResult(selectedNote);
                    dialog.close();
                }
            }
        });

        dialog.showAndWait().ifPresent(selectedNote -> {
            wikiController.setCurrentNote(selectedNote);
        });
    }

    private void editOperation(Operation operation) {
        OperationDialog dialog = new OperationDialog(operation, controller.getAllRegions());
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
        // Load notes for this specific operation
        if (wikiNoteEditor != null) {
            wikiNoteEditor.loadNotesForOperation(controller.getOperation());
        }

        // Update sidebar
        if (wikiSidebar != null) {
            wikiSidebar.refreshOperationWikis(controller.getOperation());
        }
    }
}
