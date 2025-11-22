package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class OperationTableView extends TableView<Operation> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private Consumer<Operation> onEdit;
    private Consumer<Operation> onDelete;
    private Consumer<Operation> onOperationClick;

    public OperationTableView() {
        initialize();
    }

    private void initialize() {
        // Table configuration
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        // Create columns
        TableColumn<Operation, String> nameCol = createNameColumn();
        TableColumn<Operation, OperationStatus> statusCol = createStatusColumn();
        TableColumn<Operation, Priority> priorityCol = createPriorityColumn();
        TableColumn<Operation, LocalDate> dueDateCol = createDueDateColumn();
        TableColumn<Operation, Void> actionsCol = createActionsColumn();

        getColumns().addAll(nameCol, statusCol, priorityCol, dueDateCol, actionsCol);

        // Row height
        setFixedCellSize(50);

        // Single-click to navigate, double-click to edit
        setRowFactory(tv -> {
            TableRow<Operation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (onEdit != null) {
                        onEdit.accept(row.getItem());
                    }
                } else if (event.getClickCount() == 1 && !row.isEmpty()) {
                    // Single click - navigate to detail
                    if (onOperationClick != null) {
                        onOperationClick.accept(row.getItem());
                    }
                }
            });
            return row;
        });

        // Selection styling
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private TableColumn<Operation, String> createNameColumn() {
        TableColumn<Operation, String> col = new TableColumn<>("Name");
        col.setCellValueFactory(new PropertyValueFactory<>("name"));
        col.setPrefWidth(200);
        col.setStyle("-fx-alignment: CENTER-LEFT;");
        return col;
    }

    private TableColumn<Operation, OperationStatus> createStatusColumn() {
        TableColumn<Operation, OperationStatus> col = new TableColumn<>("Status");
        col.setCellValueFactory(new PropertyValueFactory<>("status"));
        col.setPrefWidth(120);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(OperationStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = createStatusBadge(item);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        return col;
    }

    private TableColumn<Operation, Priority> createPriorityColumn() {
        TableColumn<Operation, Priority> col = new TableColumn<>("Priority");
        col.setCellValueFactory(new PropertyValueFactory<>("priority"));
        col.setPrefWidth(100);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = createPriorityBadge(item);
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        return col;
    }

    private TableColumn<Operation, LocalDate> createDueDateColumn() {
        TableColumn<Operation, LocalDate> col = new TableColumn<>("Due Date");
        col.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        col.setPrefWidth(120);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("No date set");
                    setStyle("-fx-text-fill: -roam-text-hint; -fx-font-style: italic; -fx-alignment: CENTER;");
                } else {
                    setText(DATE_FORMATTER.format(item));
                    setStyle("-fx-text-fill: -roam-text-primary; -fx-alignment: CENTER;");
                }
            }
        });
        return col;
    }

    private TableColumn<Operation, Void> createActionsColumn() {
        TableColumn<Operation, Void> col = new TableColumn<>("Actions");
        col.setPrefWidth(80);
        col.setSortable(false);
        col.setCellFactory(tc -> new TableCell<>() {
            private final Button menuBtn = createKebabMenuButton();
            private final HBox container = new HBox(menuBtn);

            {
                container.setAlignment(Pos.CENTER);

                menuBtn.setOnAction(e -> {
                    Operation op = getTableView().getItems().get(getIndex());
                    showContextMenu(op, menuBtn);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
        return col;
    }

    private Button createKebabMenuButton() {
        Button btn = new Button("⋮");
        btn.getStyleClass().add("icon-button-small");
        btn.setPrefSize(28, 28);
        btn.setTooltip(new Tooltip("More actions"));
        return btn;
    }

    private void showContextMenu(Operation operation, Button sourceButton) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✎ Edit");
        editItem.setOnAction(e -> {
            if (onEdit != null)
                onEdit.accept(operation);
        });

        MenuItem deleteItem = new MenuItem("🗑 Delete");
        deleteItem.setStyle("-fx-text-fill: #D32F2F;");
        deleteItem.setOnAction(e -> {
            if (onDelete != null)
                onDelete.accept(operation);
        });

        contextMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        contextMenu.show(sourceButton, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private Label createStatusBadge(OperationStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins SemiBold", 11));
        badge.setStyle("-fx-padding: 5 12 5 12; -fx-background-radius: 4;");

        switch (status) {
            case ONGOING -> {
                badge.setText("Ongoing");
                badge.getStyleClass().add("status-ongoing");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.getStyleClass().add("status-in-progress");
            }
            case END -> {
                badge.setText("Completed");
                badge.getStyleClass().add("status-end");
            }
        }

        return badge;
    }

    private Label createPriorityBadge(Priority priority) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Bold", 11));
        badge.setStyle("-fx-padding: 5 12 5 12; -fx-background-radius: 4;");

        switch (priority) {
            case HIGH -> {
                badge.setText("High");
                badge.getStyleClass().add("priority-high");
            }
            case MEDIUM -> {
                badge.setText("Medium");
                badge.getStyleClass().add("priority-medium");
            }
            case LOW -> {
                badge.setText("Low");
                badge.getStyleClass().add("priority-low");
            }
        }

        return badge;
    }

    public void setOnEdit(Consumer<Operation> handler) {
        this.onEdit = handler;
    }

    public void setOnDelete(Consumer<Operation> handler) {
        this.onDelete = handler;
    }

    public void setOnOperationClick(Consumer<Operation> handler) {
        this.onOperationClick = handler;
    }
}
