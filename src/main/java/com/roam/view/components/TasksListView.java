package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.*;
import com.roam.view.components.cells.EditableDateCell;
import com.roam.view.components.cells.EditablePriorityCell;
import com.roam.view.components.cells.EditableStatusCell;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TasksListView extends TableView<Task> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final TasksController controller;

    public TasksListView(TasksController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setStyle(
                "-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px; -fx-background-color: -roam-bg-primary; -fx-control-inner-background: -roam-bg-primary; -fx-text-fill: -roam-text-primary;");
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setFixedCellSize(50);
        setEditable(true);

        // Create columns
        TableColumn<Task, Boolean> checkboxCol = createCheckboxColumn();
        TableColumn<Task, Priority> priorityCol = createPriorityIndicatorColumn();
        TableColumn<Task, String> titleCol = createTitleColumn();
        TableColumn<Task, String> operationCol = createOperationColumn();
        TableColumn<Task, TaskStatus> statusCol = createStatusColumn();
        TableColumn<Task, Priority> priorityBadgeCol = createPriorityBadgeColumn();
        TableColumn<Task, String> assigneeCol = createAssigneeColumn();
        TableColumn<Task, LocalDateTime> dueDateCol = createDueDateColumn();
        TableColumn<Task, Void> actionsCol = createActionsColumn();

        getColumns().addAll(
                checkboxCol, priorityCol, titleCol, operationCol,
                statusCol, priorityBadgeCol, assigneeCol, dueDateCol, actionsCol);
    }

    private TableColumn<Task, Boolean> createCheckboxColumn() {
        TableColumn<Task, Boolean> col = new TableColumn<>();
        col.setPrefWidth(40);
        col.setMaxWidth(40);
        col.setMinWidth(40);
        col.setSortable(false);
        col.setResizable(false);

        // Master checkbox in header
        CheckBox masterCheckbox = new CheckBox();
        col.setGraphic(masterCheckbox);

        masterCheckbox.setOnAction(e -> {
            if (masterCheckbox.isSelected()) {
                controller.selectAll(controller.loadTasks());
            } else {
                controller.clearSelection();
            }
        });

        col.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    controller.toggleTaskSelection(task);
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    checkBox.setSelected(controller.getSelectedTasks().contains(task));
                    setGraphic(checkBox);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        return col;
    }

    private TableColumn<Task, Priority> createPriorityIndicatorColumn() {
        TableColumn<Task, Priority> col = new TableColumn<>();
        col.setPrefWidth(8);
        col.setMaxWidth(8);
        col.setMinWidth(8);
        col.setSortable(false);
        col.setResizable(false);
        col.setCellValueFactory(new PropertyValueFactory<>("priority"));

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Region bar = new Region();
                    bar.setPrefWidth(4);
                    bar.setPrefHeight(50);
                    bar.setStyle("-fx-background-color: " + getPriorityColor(item) + ";");
                    setGraphic(bar);
                    setPadding(new Insets(0));
                }
            }
        });

        return col;
    }

    private TableColumn<Task, String> createTitleColumn() {
        TableColumn<Task, String> col = new TableColumn<>("Title");
        col.setCellValueFactory(new PropertyValueFactory<>("title"));
        col.setPrefWidth(250);

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());

                    Label titleLabel = new Label(item);
                    titleLabel.setFont(Font.font("Poppins Medium", 14));
                    titleLabel.setStyle("-fx-text-fill: -roam-text-primary; -fx-cursor: hand;");
                    titleLabel.setOnMouseClicked(e -> controller.editTask(task));

                    setGraphic(titleLabel);
                }
            }
        });

        return col;
    }

    private TableColumn<Task, String> createOperationColumn() {
        TableColumn<Task, String> col = new TableColumn<>("Operation");
        col.setPrefWidth(150);

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    if (task.getOperationId() != null) {
                        controller.getOperationById(task.getOperationId()).ifPresent(operation -> {
                            Label opBadge = new Label(operation.getName());
                            opBadge.setFont(Font.font("Poppins Regular", 11));
                            opBadge.setStyle(
                                    "-fx-background-color: -roam-blue-light; " +
                                            "-fx-text-fill: -roam-blue; " +
                                            "-fx-padding: 3 8 3 8; " +
                                            "-fx-background-radius: 10;");
                            setGraphic(opBadge);
                        });
                    } else {
                        setText("—");
                        setGraphic(null);
                    }
                }
            }
        });

        return col;
    }

    private TableColumn<Task, TaskStatus> createStatusColumn() {
        TableColumn<Task, TaskStatus> col = new TableColumn<>("Status");
        col.setCellValueFactory(new PropertyValueFactory<>("status"));
        col.setPrefWidth(120);
        col.setCellFactory(tc -> new EditableStatusCell(controller));
        col.setEditable(true);
        return col;
    }

    private TableColumn<Task, Priority> createPriorityBadgeColumn() {
        TableColumn<Task, Priority> col = new TableColumn<>("Priority");
        col.setCellValueFactory(new PropertyValueFactory<>("priority"));
        col.setPrefWidth(100);
        col.setCellFactory(tc -> new EditablePriorityCell(controller));
        col.setEditable(true);
        return col;
    }

    private TableColumn<Task, String> createAssigneeColumn() {
        TableColumn<Task, String> col = new TableColumn<>("Assignee");
        col.setCellValueFactory(new PropertyValueFactory<>("assignee"));
        col.setPrefWidth(100);

        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (item == null || item.isEmpty()) {
                    setText("—");
                    setStyle("-fx-text-fill: -roam-text-hint; -fx-font-style: italic;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: -roam-text-primary;");
                }
            }
        });

        return col;
    }

    private TableColumn<Task, LocalDateTime> createDueDateColumn() {
        TableColumn<Task, LocalDateTime> col = new TableColumn<>("Due Date");
        col.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        col.setPrefWidth(120);
        col.setCellFactory(tc -> new EditableDateCell(controller));
        col.setEditable(true);
        return col;
    }

    private TableColumn<Task, Void> createActionsColumn() {
        TableColumn<Task, Void> col = new TableColumn<>("Actions");
        col.setPrefWidth(80);
        col.setSortable(false);

        col.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = createActionButton(Feather.EDIT_2, "Edit task");
            private final Button deleteBtn = createActionButton(Feather.TRASH_2, "Delete task");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    controller.editTask(task);
                });

                deleteBtn.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    controller.deleteTask(task.getId());
                });

                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                        "-fx-background-color: #FFEBEE; -fx-cursor: hand; -fx-background-radius: 4;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                        "-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 4;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        return col;
    }

    private Button createActionButton(Feather iconCode, String tooltip) {
        Button btn = new Button();
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize(16);
        btn.setGraphic(icon);
        btn.setPrefSize(28, 28);
        btn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 4;");
        btn.setTooltip(new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: -roam-gray-bg; -fx-cursor: hand; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 4;"));

        return btn;
    }

    private Label createStatusBadge(TaskStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Regular", 12));
        badge.setStyle("-fx-padding: 4 12 4 12; -fx-background-radius: 12;");

        switch (status) {
            case TODO -> {
                badge.setText("To Do");
                badge.setStyle(badge.getStyle() + "-fx-background-color: -roam-blue-light; -fx-text-fill: -roam-blue;");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #FFF3E0; -fx-text-fill: #F57C00;");
            }
            case DONE -> {
                badge.setText("Done");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #E8F5E9; -fx-text-fill: #388E3C;");
            }
        }

        return badge;
    }

    private Label createPriorityBadge(Priority priority) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Regular", 12));
        badge.setStyle("-fx-padding: 4 12 4 12; -fx-background-radius: 12;");

        switch (priority) {
            case HIGH -> {
                badge.setText("High");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828;");
            }
            case MEDIUM -> {
                badge.setText("Medium");
                badge.setStyle(badge.getStyle() + "-fx-background-color: #FFF8E1; -fx-text-fill: #F9A825;");
            }
            case LOW -> {
                badge.setText("Low");
                badge.setStyle(
                        badge.getStyle() + "-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            }
        }

        return badge;
    }

    private String getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> "#C62828";
            case MEDIUM -> "#F9A825";
            case LOW -> "#616161";
        };
    }

    public void loadTasks(List<Task> tasks) {
        getItems().setAll(tasks);
    }
}
