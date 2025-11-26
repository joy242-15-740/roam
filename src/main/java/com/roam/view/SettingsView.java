package com.roam.view;

import com.roam.controller.JournalController;
import com.roam.controller.WikiController;
import com.roam.model.JournalTemplate;
import com.roam.model.Settings;
import com.roam.model.WikiTemplate;
import com.roam.service.DataService;
import com.roam.service.SearchService;
import com.roam.service.SecurityContext;
import com.roam.service.SettingsService;
import com.roam.util.ExportUtils;
import com.roam.util.ImportUtils;
import com.roam.util.StyleBuilder;
import com.roam.util.ThemeManager;
import com.roam.util.ThreadPoolManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.roam.util.UIConstants.*;

/**
 * Settings view for managing application preferences, security, and data.
 */
public class SettingsView extends ScrollPane {

    private final SettingsService settingsService;
    private final SecurityContext securityContext;
    private final DataService dataService;
    private final WikiController wikiController;
    private final JournalController journalController;
    private final com.roam.controller.CalendarController calendarController;
    private final com.roam.controller.OperationsController operationsController;

    public SettingsView() {
        this.settingsService = SettingsService.getInstance();
        this.securityContext = SecurityContext.getInstance();
        this.dataService = new DataService();
        this.wikiController = new WikiController();
        this.journalController = new JournalController();
        this.calendarController = new com.roam.controller.CalendarController();
        this.operationsController = new com.roam.controller.OperationsController();
        initialize();
    }

    private void initialize() {
        VBox content = new VBox();
        content.setPadding(new Insets(SPACING_SECTION));
        content.setSpacing(SPACING_LG);
        content.setAlignment(Pos.TOP_LEFT);

        // Configure ScrollPane
        setContent(content);
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setHbarPolicy(ScrollBarPolicy.NEVER);

        Label header = new Label("Settings");
        header.setFont(Font.font(FONT_BOLD, FONT_SIZE_HEADER));

        // Security Section
        Label securityHeader = new Label("Security");
        securityHeader.setFont(Font.font(FONT_BOLD, FONT_SIZE_XL));

        CheckBox lockToggle = new CheckBox("Enable Lock Screen");
        lockToggle.setSelected(securityContext.isLockEnabled());
        lockToggle.setOnAction(e -> handleLockToggle(lockToggle));

        Button changePinBtn = new Button("Change PIN");
        changePinBtn.setOnAction(e -> handleChangePin());
        changePinBtn.disableProperty().bind(lockToggle.selectedProperty().not());

        VBox securityBox = new VBox(10, securityHeader, lockToggle, changePinBtn);
        securityBox.setStyle(StyleBuilder.sectionStyle());

        // Appearance Section
        Label appearanceHeader = new Label("Appearance");
        appearanceHeader.setFont(Font.font(FONT_BOLD, FONT_SIZE_XL));

        ComboBox<String> themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll("Light", "Dark");
        themeSelector.setValue(settingsService.getSettings().getTheme());
        themeSelector.setOnAction(e -> handleThemeChange(themeSelector.getValue()));

        VBox appearanceBox = new VBox(10, appearanceHeader, new Label("Theme"), themeSelector);
        appearanceBox.setStyle(StyleBuilder.sectionStyle());

        // Data Management Section
        Label dataHeader = new Label("Data Management");
        dataHeader.setFont(Font.font(FONT_BOLD, FONT_SIZE_XL));

        Button exportBtn = new Button("Export Data (JSON)");
        exportBtn.setStyle(createActionButtonStyle(BLUE));
        exportBtn.setOnAction(e -> handleExport());

        Button importBtn = new Button("Import Data (JSON)");
        importBtn.setStyle(createActionButtonStyle(GREEN));
        importBtn.setOnAction(e -> handleImport());

        Button rebuildIndexBtn = new Button("Rebuild Search Index");
        rebuildIndexBtn.setStyle(createActionButtonStyle(ORANGE));
        rebuildIndexBtn.setOnAction(e -> handleRebuildIndex());

        Label dataWarning = new Label("⚠️ Import will merge with existing data. Backup first!");
        dataWarning.setFont(Font.font(FONT_REGULAR, FONT_SIZE_SM));
        dataWarning.setStyle("-fx-text-fill: " + ORANGE + ";");

        Label indexInfo = new Label("Rebuild search index to update search results with all content.");
        indexInfo.setFont(Font.font(FONT_REGULAR, FONT_SIZE_SM));
        indexInfo.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");

        HBox buttonBox = new HBox(10, exportBtn, importBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Specific Exports
        Label exportHeader = new Label("Export Content");
        exportHeader.setFont(Font.font(FONT_MEDIUM, FONT_SIZE_STANDARD));

        Button exportWikisBtn = new Button("Export Wikis (MD)");
        exportWikisBtn.setOnAction(e -> handleExportWikis());

        Button exportEventsBtn = new Button("Export Events (ICS)");
        exportEventsBtn.setOnAction(e -> handleExportEvents());

        Button exportOpsBtn = new Button("Export Operations (JSON)");
        exportOpsBtn.setOnAction(e -> handleExportOperations());

        Button exportJournalsBtn = new Button("Export Journals (MD)");
        exportJournalsBtn.setOnAction(e -> handleExportJournals());

        HBox specificExportsBox = new HBox(10, exportWikisBtn, exportEventsBtn, exportOpsBtn, exportJournalsBtn);
        specificExportsBox.setAlignment(Pos.CENTER_LEFT);

        // Specific Imports
        Label importHeader = new Label("Import Content");
        importHeader.setFont(Font.font(FONT_MEDIUM, FONT_SIZE_STANDARD));

        Button importWikisBtn = new Button("Import Wikis (MD)");
        importWikisBtn.setOnAction(e -> handleImportWikis());

        Button importEventsBtn = new Button("Import Events (ICS)");
        importEventsBtn.setOnAction(e -> handleImportEvents());

        Button importOpsBtn = new Button("Import Operations (JSON)");
        importOpsBtn.setOnAction(e -> handleImportOperations());

        Button importJournalsBtn = new Button("Import Journals (MD)");
        importJournalsBtn.setOnAction(e -> handleImportJournals());

        HBox specificImportsBox = new HBox(10, importWikisBtn, importEventsBtn, importOpsBtn, importJournalsBtn);
        specificImportsBox.setAlignment(Pos.CENTER_LEFT);

        VBox dataBox = new VBox(10, dataHeader, buttonBox, dataWarning, rebuildIndexBtn, indexInfo, new Separator(),
                exportHeader, specificExportsBox, new Separator(), importHeader, specificImportsBox);
        dataBox.setStyle(StyleBuilder.sectionStyle());

        // Templates Section
        Label templatesHeader = new Label("Templates");
        templatesHeader.setFont(Font.font(FONT_BOLD, FONT_SIZE_XL));

        VBox wikiTemplatesBox = createWikiTemplatesBox();
        VBox journalTemplatesBox = createJournalTemplatesBox();

        VBox templatesBox = new VBox(10, templatesHeader, wikiTemplatesBox, journalTemplatesBox);
        templatesBox.setStyle(StyleBuilder.sectionStyle());

        content.getChildren().addAll(header, securityBox, appearanceBox, dataBox, templatesBox);
    }

    /**
     * Creates a consistent style for action buttons.
     */
    private String createActionButtonStyle(String bgColor) {
        return StyleBuilder.create()
                .backgroundColor(bgColor)
                .textFill(TEXT_WHITE)
                .padding(10, 20)
                .cursorHand()
                .build();
    }

    private void handleLockToggle(CheckBox toggle) {
        boolean isEnabled = toggle.isSelected();
        if (isEnabled) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Set PIN");
            dialog.setHeaderText("Enter a new PIN to enable lock screen");
            dialog.setContentText("PIN (minimum " + SecurityContext.getMinPinLength() + " characters):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                try {
                    securityContext.setPin(result.get());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "PIN set successfully! Lock screen enabled.", ButtonType.OK);
                    alert.showAndWait();
                } catch (IllegalArgumentException e) {
                    // Show error if PIN is too short
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    errorAlert.setTitle("Invalid PIN");
                    errorAlert.showAndWait();
                    toggle.setSelected(false); // Revert toggle
                }
            } else {
                toggle.setSelected(false); // Revert if cancelled
            }
        } else {
            securityContext.disableLock();
        }
    }

    private void handleChangePin() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change PIN");
        dialog.setHeaderText("Enter new PIN (minimum " + SecurityContext.getMinPinLength() + " characters)");
        dialog.setContentText("New PIN:");

        dialog.showAndWait().ifPresent(newPin -> {
            if (!newPin.isEmpty()) {
                try {
                    securityContext.setPin(newPin);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "PIN updated successfully!", ButtonType.OK);
                    alert.showAndWait();
                } catch (IllegalArgumentException e) {
                    // Show error if PIN is too short
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    errorAlert.setTitle("Invalid PIN");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void handleThemeChange(String theme) {
        Settings settings = settingsService.getSettings();
        settings.setTheme(theme);
        settingsService.saveSettings();

        // Apply theme using ThemeManager (handles both AtlantaFX theme and dark class)
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.applyTheme(theme);

        // Also update current scene's root
        if (getScene() != null) {
            themeManager.applyDarkClass(getScene().getRoot(), themeManager.isDarkTheme());
        }
    }

    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.setInitialFileName(
                "roam-backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
                        + ".json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            // Show progress dialog
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Exporting Data");
            progressAlert.setHeaderText("Please wait...");
            progressAlert.setContentText("Exporting your data to JSON file.");
            progressAlert.show();

            // Create JavaFX Task for background export
            Task<DataService.ExportResult> exportTask = new Task<>() {
                @Override
                protected DataService.ExportResult call() {
                    return dataService.exportData(file);
                }
            };

            // Handle task completion
            exportTask.setOnSucceeded(event -> {
                progressAlert.close();
                DataService.ExportResult result = exportTask.getValue();

                if (result.isSuccess()) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Export Successful");
                    successAlert.setHeaderText("Data exported successfully!");
                    successAlert.setContentText(
                            "Exported " + result.getRecordCount() + " records to:\n" + result.getFilePath());
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Export Failed");
                    errorAlert.setHeaderText("Failed to export data");
                    errorAlert.setContentText(result.getMessage());
                    errorAlert.showAndWait();
                }
            });

            exportTask.setOnFailed(event -> {
                progressAlert.close();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Export Failed");
                errorAlert.setHeaderText("An error occurred during export");
                errorAlert.setContentText(exportTask.getException().getMessage());
                errorAlert.showAndWait();
            });

            // Submit to I/O thread pool
            ThreadPoolManager.getInstance().submitIoTask(exportTask);
        }
    }

    private void handleImport() {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Import Data");
        confirmAlert.setHeaderText("Import data from JSON file");
        confirmAlert.setContentText(
                "This will import data and merge it with your existing data.\n\n" +
                        "Duplicates will be skipped based on names.\n\n" +
                        "Do you want to continue?");

        Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            // Show progress dialog
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Importing Data");
            progressAlert.setHeaderText("Please wait...");
            progressAlert.setContentText("Importing data from JSON file.");
            progressAlert.show();

            // Create JavaFX Task for background import
            Task<DataService.ImportResult> importTask = new Task<>() {
                @Override
                protected DataService.ImportResult call() {
                    return dataService.importData(file, true); // true = merge mode
                }
            };

            // Handle task completion
            importTask.setOnSucceeded(event -> {
                progressAlert.close();
                DataService.ImportResult result = importTask.getValue();

                if (result.isSuccess()) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Import Successful");
                    successAlert.setHeaderText("Data imported successfully!");
                    successAlert.setContentText(
                            "Imported: " + result.getImportedCount() + " records\n" +
                                    "Skipped: " + result.getSkippedCount() + " duplicates\n\n" +
                                    result.getMessage());
                    successAlert.showAndWait();

                    // Suggest restart
                    Alert restartAlert = new Alert(Alert.AlertType.INFORMATION);
                    restartAlert.setTitle("Restart Required");
                    restartAlert.setHeaderText("Please restart the application");
                    restartAlert.setContentText(
                            "To see the imported data, please restart Roam.");
                    restartAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Import Failed");
                    errorAlert.setHeaderText("Failed to import data");
                    errorAlert.setContentText(result.getMessage());
                    errorAlert.showAndWait();
                }
            });

            importTask.setOnFailed(event -> {
                progressAlert.close();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Import Failed");
                errorAlert.setHeaderText("An error occurred during import");
                errorAlert.setContentText(importTask.getException().getMessage());
                errorAlert.showAndWait();
            });

            // Submit to I/O thread pool
            ThreadPoolManager.getInstance().submitIoTask(importTask);
        }
    }

    private void handleRebuildIndex() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Rebuild Search Index");
        confirmAlert.setHeaderText("Rebuild the search index?");
        confirmAlert.setContentText(
                "This will reindex all Wikis, Tasks, Operations, Events, and Journal entries.\n\n" +
                        "This may take a few moments depending on the amount of data.\n\n" +
                        "Do you want to continue?");

        Optional<ButtonType> confirmResult = confirmAlert.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        // Show progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Rebuilding Index");
        progressAlert.setHeaderText("Please wait...");
        progressAlert.setContentText("Reindexing all content for search.");
        progressAlert.show();

        // Create JavaFX Task for background index rebuild
        Task<Integer> rebuildTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                SearchService searchService = SearchService.getInstance();

                // Clear existing index
                searchService.clearIndex();

                // Reindex all content
                com.roam.repository.WikiRepository wikiRepo = new com.roam.repository.WikiRepository();
                com.roam.repository.TaskRepository taskRepo = new com.roam.repository.TaskRepository();
                com.roam.repository.OperationRepository opRepo = new com.roam.repository.OperationRepository();
                com.roam.repository.CalendarEventRepository eventRepo = new com.roam.repository.CalendarEventRepository();
                com.roam.repository.JournalEntryRepository journalRepo = new com.roam.repository.JournalEntryRepository();

                int count = 0;

                // Index all wikis
                for (com.roam.model.Wiki wiki : wikiRepo.findAll()) {
                    searchService.indexWiki(
                            wiki.getId(),
                            wiki.getTitle(),
                            wiki.getContent(),
                            wiki.getRegion(),
                            wiki.getOperationId(),
                            wiki.getUpdatedAt());
                    count++;
                }

                // Index all tasks
                for (com.roam.model.Task task : taskRepo.findAll()) {
                    searchService.indexTask(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getPriority() != null ? task.getPriority().toString() : null,
                            task.getStatus() != null ? task.getStatus().toString() : null,
                            task.getOperationId(),
                            task.getDueDate());
                    count++;
                }

                // Index all operations
                for (com.roam.model.Operation op : opRepo.findAll()) {
                    searchService.indexOperation(
                            op.getId(),
                            op.getName(),
                            op.getPurpose(),
                            op.getOutcome(),
                            op.getStatus() != null ? op.getStatus().toString() : null,
                            op.getPriority() != null ? op.getPriority().toString() : null);
                    count++;
                }

                // Index all events
                for (com.roam.model.CalendarEvent event : eventRepo.findAll()) {
                    searchService.indexEvent(
                            event.getId(),
                            event.getTitle(),
                            event.getDescription(),
                            event.getStartDateTime(),
                            event.getEndDateTime(),
                            event.getLocation());
                    count++;
                }

                // Index all journal entries
                for (com.roam.model.JournalEntry entry : journalRepo.findAll()) {
                    searchService.indexJournalEntry(
                            entry.getId(),
                            entry.getTitle(),
                            entry.getContent(),
                            entry.getDate() != null ? entry.getDate().toString() : null);
                    count++;
                }

                return count;
            }
        };

        // Handle task completion
        rebuildTask.setOnSucceeded(event -> {
            progressAlert.close();
            int totalCount = rebuildTask.getValue();

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Index Rebuilt");
            successAlert.setHeaderText("Search index rebuilt successfully!");
            successAlert.setContentText(
                    "Indexed " + totalCount + " items.\n\n" +
                            "You can now search across all your content.");
            successAlert.showAndWait();
        });

        rebuildTask.setOnFailed(event -> {
            progressAlert.close();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Rebuild Failed");
            errorAlert.setHeaderText("Failed to rebuild search index");
            errorAlert.setContentText(rebuildTask.getException().getMessage());
            errorAlert.showAndWait();
        });

        // Submit to compute thread pool (CPU-intensive indexing)
        ThreadPoolManager.getInstance().submitComputeTask(rebuildTask);
    }

    private VBox createWikiTemplatesBox() {
        Label header = new Label("Wiki Templates");
        header.setFont(Font.font("Poppins Medium", 14));

        ListView<WikiTemplate> listView = new ListView<>();
        listView.getItems().addAll(wikiController.loadAllTemplates());
        listView.setPrefHeight(150);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WikiTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIcon() + " " + item.getName() + (item.getIsDefault() ? " (Default)" : ""));
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> handleCreateWikiTemplate(listView));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> handleEditWikiTemplate(listView));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> handleDeleteWikiTemplate(listView));

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        return new VBox(5, header, listView, buttons);
    }

    private VBox createJournalTemplatesBox() {
        Label header = new Label("Journal Templates");
        header.setFont(Font.font("Poppins Medium", 14));

        ListView<JournalTemplate> listView = new ListView<>();
        listView.getItems().addAll(journalController.loadTemplates());
        listView.setPrefHeight(150);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(JournalTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> handleCreateJournalTemplate(listView));

        Button editBtn = new Button("Edit");
        editBtn.setOnAction(e -> handleEditJournalTemplate(listView));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> handleDeleteJournalTemplate(listView));

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        return new VBox(5, header, listView, buttons);
    }

    private void handleCreateWikiTemplate(ListView<WikiTemplate> listView) {
        Dialog<WikiTemplate> dialog = new Dialog<>();
        dialog.setTitle("New Wiki Template");
        dialog.setHeaderText("Create a new wiki template");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField();
        name.setPromptText("Template Name");
        TextField icon = new TextField();
        icon.setPromptText("Icon (emoji)");
        TextArea content = new TextArea();
        content.setPromptText("Content (supports {date}, {time}, {title})");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Icon:"), 0, 1);
        grid.add(createStyledInput(icon, Feather.SMILE), 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(content, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return wikiController.createTemplate(name.getText(), "", content.getText(), icon.getText());
            }
            return null;
        });

        Optional<WikiTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.getItems().add(template));
    }

    private void handleDeleteWikiTemplate(ListView<WikiTemplate> listView) {
        WikiTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            wikiController.deleteTemplate(selected);
            listView.getItems().remove(selected);
        }
    }

    private void handleCreateJournalTemplate(ListView<JournalTemplate> listView) {
        Dialog<JournalTemplate> dialog = new Dialog<>();
        dialog.setTitle("New Journal Template");
        dialog.setHeaderText("Create a new journal template");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField();
        name.setPromptText("Template Name");
        TextArea content = new TextArea();
        content.setPromptText("Content");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(content, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return journalController.createTemplate(name.getText(), content.getText());
            }
            return null;
        });

        Optional<JournalTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.getItems().add(template));
    }

    private void handleDeleteJournalTemplate(ListView<JournalTemplate> listView) {
        JournalTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            journalController.deleteTemplate(selected);
            listView.getItems().remove(selected);
        }
    }

    private void handleEditWikiTemplate(ListView<WikiTemplate> listView) {
        WikiTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<WikiTemplate> dialog = new Dialog<>();
        dialog.setTitle("Edit Wiki Template");
        dialog.setHeaderText("Edit wiki template");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField(selected.getName());
        name.setPromptText("Template Name");
        TextField icon = new TextField(selected.getIcon());
        icon.setPromptText("Icon (emoji)");
        TextArea content = new TextArea(selected.getContent());
        content.setPromptText("Content (supports {date}, {time}, {title})");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Icon:"), 0, 1);
        grid.add(createStyledInput(icon, Feather.SMILE), 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(content, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setName(name.getText());
                selected.setIcon(icon.getText());
                selected.setContent(content.getText());
                return wikiController.updateTemplate(selected);
            }
            return null;
        });

        Optional<WikiTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.refresh());
    }

    private void handleEditJournalTemplate(ListView<JournalTemplate> listView) {
        JournalTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<JournalTemplate> dialog = new Dialog<>();
        dialog.setTitle("Edit Journal Template");
        dialog.setHeaderText("Edit journal template");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField(selected.getName());
        name.setPromptText("Template Name");
        TextArea content = new TextArea(selected.getContent());
        content.setPromptText("Content");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(content, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setName(name.getText());
                selected.setContent(content.getText());
                return journalController.updateTemplate(selected);
            }
            return null;
        });

        Optional<JournalTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.refresh());
    }

    private void handleExportWikis() {
        java.util.List<com.roam.model.Wiki> allNotes = wikiController.loadAllNotes();
        ExportUtils.exportAllNotesToMarkdown(getScene().getWindow(), allNotes);
    }

    private void handleExportEvents() {
        java.util.List<com.roam.model.CalendarEvent> events = calendarController.getAllEvents();
        ExportUtils.exportEventsToICS(getScene().getWindow(), events);
    }

    private void handleExportOperations() {
        java.util.List<com.roam.model.Operation> operations = operationsController.loadOperations();
        ExportUtils.exportOperationsToJSON(getScene().getWindow(), operations);
    }

    private void handleExportJournals() {
        java.util.List<com.roam.model.JournalEntry> entries = journalController.loadAllEntries();
        ExportUtils.exportJournalsToMarkdown(getScene().getWindow(), entries);
    }

    private void handleImportWikis() {
        ImportUtils.importNotesFromMarkdown(getScene().getWindow());
    }

    private void handleImportEvents() {
        ImportUtils.importEventsFromICS(getScene().getWindow());
    }

    private void handleImportOperations() {
        ImportUtils.importOperationsFromJSON(getScene().getWindow());
    }

    private void handleImportJournals() {
        ImportUtils.importJournalsFromMarkdown(getScene().getWindow());
    }

    private StackPane createStyledInput(TextField textField, Feather icon) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconColor(javafx.scene.paint.Color.GRAY);
        fontIcon.setIconSize(16);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER_LEFT);
        stackPane.getChildren().addAll(textField, fontIcon);

        // Add padding to TextField to make room for icon
        textField.setStyle("-fx-padding: 5 5 5 30;");

        // Add margin to icon
        StackPane.setMargin(fontIcon, new Insets(0, 0, 0, 10));

        // Make icon mouse transparent so clicks go to TextField
        fontIcon.setMouseTransparent(true);

        return stackPane;
    }
}
