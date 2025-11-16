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

    public OperationTableView() {
        initialize();
    }

    private void initialize() {
        // Table configuration
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setStyle("-fx-font-family: 'Poppins Regular'; -fx-font-size: 14px;");

        // Create columns
        TableColumn<Operation, String> nameCol = createNameColumn();
        TableColumn<Operation, String> purposeCol = createPurposeColumn();
        TableColumn<Operation, OperationStatus> statusCol = createStatusColumn();
        TableColumn<Operation, Priority> priorityCol = createPriorityColumn();
        TableColumn<Operation, LocalDate> dueDateCol = createDueDateColumn();
        TableColumn<Operation, Void> actionsCol = createActionsColumn();

        getColumns().addAll(nameCol, purposeCol, statusCol, priorityCol, dueDateCol, actionsCol);

        // Row height
        setFixedCellSize(50);

        // Double-click to edit
        setRowFactory(tv -> {
            TableRow<Operation> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (onEdit != null) {
                        onEdit.accept(row.getItem());
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

    private TableColumn<Operation, String> createPurposeColumn() {
        TableColumn<Operation, String> col = new TableColumn<>("Purpose");
        col.setCellValueFactory(new PropertyValueFactory<>("purpose"));
        col.setPrefWidth(250);
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    // Truncate at 50 characters
                    String display = item.length() > 50 ? item.substring(0, 50) + "..." : item;
                    setText(display);
                    setStyle("-fx-alignment: CENTER-LEFT;");
                }
            }
        });
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
                    setStyle("-fx-text-fill: #9E9E9E; -fx-font-style: italic; -fx-alignment: CENTER;");
                } else {
                    setText(DATE_FORMATTER.format(item));
                    setStyle("-fx-text-fill: #000000; -fx-alignment: CENTER;");
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
            private final Button editBtn = createActionButton("✎", "Edit operation");
            private final Button deleteBtn = createActionButton("🗑", "Delete operation");
            private final HBox container = new HBox(5, editBtn, deleteBtn);

            {
                container.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Operation op = getTableView().getItems().get(getIndex());
                    if (onEdit != null)
                        onEdit.accept(op);
                });

                deleteBtn.setOnAction(e -> {
                    Operation op = getTableView().getItems().get(getIndex());
                    if (onDelete != null)
                        onDelete.accept(op);
                });

                // Delete button special hover
                deleteBtn.setOnMouseEntered(e -> deleteBtn
                        .setStyle("-fx-background-color: #FFEBEE; -fx-cursor: hand; -fx-background-radius: 4;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn
                        .setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-background-radius: 4;"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
        return col;
    }

    private Button createActionButton(String icon, String tooltip) {
        Button btn = new Button(icon);
        btn.setPrefSize(24, 24);
        btn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-font-size: 16px; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 4;");
        btn.setTooltip(new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #F5F5F5; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand; -fx-background-radius: 4;"));

        return btn;
    }

    private Label createStatusBadge(OperationStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Regular", 12));
        badge.setStyle("-fx-padding: 4 12 4 12; -fx-background-radius: 12;");

        switch (status) {
            case ONGOING -> {
                badge.setText("Ongoing");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FFF3E0; -fx-text-fill: #F57C00;");
            }
            case END -> {
                badge.setText("Completed");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #E8F5E9; -fx-text-fill: #388E3C;");
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
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FFEBEE; -fx-text-fill: #C62828;");
            }
            case MEDIUM -> {
                badge.setText("Medium");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #FFF8E1; -fx-text-fill: #F9A825;");
            }
            case LOW -> {
                badge.setText("Low");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: #F5F5F5; -fx-text-fill: #616161;");
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
}
