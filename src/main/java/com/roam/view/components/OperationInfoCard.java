package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class OperationInfoCard extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final Operation operation;
    private final Consumer<Operation> onEdit;

    public OperationInfoCard(Operation operation, Consumer<Operation> onEdit) {
        this.operation = operation;
        this.onEdit = onEdit;

        setSpacing(15);
        setPadding(new Insets(25));
        setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");
        VBox.setMargin(this, new Insets(0, 0, 20, 0));

        // Header row
        HBox header = createHeader();

        // Metadata row
        HBox metadata = createMetadata();

        // Add components
        getChildren().addAll(header, metadata);

        // Purpose section (if exists)
        if (operation.getPurpose() != null && !operation.getPurpose().isEmpty()) {
            VBox purposeSection = createTextSection("Purpose", operation.getPurpose());
            getChildren().add(purposeSection);
        }

        // Outcome section (if exists)
        if (operation.getOutcome() != null && !operation.getOutcome().isEmpty()) {
            VBox outcomeSection = createTextSection("Outcome", operation.getOutcome());
            getChildren().add(outcomeSection);
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Operation name
        Label nameLabel = new Label(operation.getName());
        nameLabel.setFont(Font.font("Poppins Bold", 32));
        nameLabel.setStyle("-fx-text-fill: #000000;");
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        // Edit button
        Button editBtn = new Button("✎ Edit Info");
        editBtn.setFont(Font.font("Poppins Regular", 14));
        editBtn.setStyle(
                "-fx-background-color: #F5F5F5; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: #E0E0E0; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: #F5F5F5; " +
                        "-fx-text-fill: #000000; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        editBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(operation);
            }
        });

        header.getChildren().addAll(nameLabel, editBtn);
        return header;
    }

    private HBox createMetadata() {
        HBox metadata = new HBox(20);
        metadata.setAlignment(Pos.CENTER_LEFT);

        // Status badge
        Label statusBadge = createStatusBadge(operation.getStatus());

        // Priority badge
        Label priorityBadge = createPriorityBadge(operation.getPriority());

        // Due date
        Label dueDateLabel = new Label();
        dueDateLabel.setFont(Font.font("Poppins Regular", 13));
        dueDateLabel.setStyle("-fx-text-fill: #616161;");
        if (operation.getDueDate() != null) {
            dueDateLabel.setText("📅 Due: " + DATE_FORMATTER.format(operation.getDueDate()));
        } else {
            dueDateLabel.setText("📅 No due date");
        }

        metadata.getChildren().addAll(statusBadge, priorityBadge, dueDateLabel);
        return metadata;
    }

    private VBox createTextSection(String title, String content) {
        VBox section = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins Regular", 12));
        titleLabel.setStyle("-fx-text-fill: #9E9E9E;");

        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Poppins Regular", 14));
        contentLabel.setStyle("-fx-text-fill: #000000; -fx-line-spacing: 0.6;");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private Label createStatusBadge(OperationStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Regular", 13));
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

    private Label createPriorityBadge(com.roam.model.Priority priority) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins Regular", 13));
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

    public void refresh(Operation updatedOperation) {
        // Re-create the card with updated data
        getChildren().clear();

        HBox header = createHeader();
        HBox metadata = createMetadata();

        getChildren().addAll(header, metadata);

        if (updatedOperation.getPurpose() != null && !updatedOperation.getPurpose().isEmpty()) {
            VBox purposeSection = createTextSection("Purpose", updatedOperation.getPurpose());
            getChildren().add(purposeSection);
        }

        if (updatedOperation.getOutcome() != null && !updatedOperation.getOutcome().isEmpty()) {
            VBox outcomeSection = createTextSection("Outcome", updatedOperation.getOutcome());
            getChildren().add(outcomeSection);
        }
    }
}
