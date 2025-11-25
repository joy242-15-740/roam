package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Eisenhower Matrix View - classifies tasks into 4 quadrants:
 * 1. Urgent & Important (Do First) - High Priority + Due Soon
 * 2. Not Urgent but Important (Schedule) - High/Medium Priority + Due Later
 * 3. Urgent but Not Important (Delegate) - Low Priority + Due Soon
 * 4. Not Urgent & Not Important (Eliminate) - Low Priority + Due Later
 */
public class TasksEisenhowerView extends GridPane {

    private final TasksController controller;

    // Quadrant containers
    private VBox urgentImportantContainer;
    private VBox notUrgentImportantContainer;
    private VBox urgentNotImportantContainer;
    private VBox notUrgentNotImportantContainer;

    // Count labels
    private Label urgentImportantCount;
    private Label notUrgentImportantCount;
    private Label urgentNotImportantCount;
    private Label notUrgentNotImportantCount;

    // Urgency threshold (tasks due within 3 days are considered urgent)
    private static final int URGENT_DAYS_THRESHOLD = 3;

    public TasksEisenhowerView(TasksController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        setHgap(20);
        setVgap(20);
        setPadding(new Insets(20));
        // Remove manual background color to let theme handle it
        // setStyle("-fx-background-color: -roam-bg-primary;");

        // Create quadrants
        VBox urgentImportant = createQuadrant(
                "DO FIRST",
                "Urgent & Important",
                "danger", // Use semantic color names for mapping later or CSS classes
                urgentImportantContainer = new VBox(10),
                urgentImportantCount = new Label("0"));

        VBox notUrgentImportant = createQuadrant(
                "SCHEDULE",
                "Not Urgent but Important",
                "accent",
                notUrgentImportantContainer = new VBox(10),
                notUrgentImportantCount = new Label("0"));

        VBox urgentNotImportant = createQuadrant(
                "DELEGATE",
                "Urgent but Not Important",
                "warning",
                urgentNotImportantContainer = new VBox(10),
                urgentNotImportantCount = new Label("0"));

        VBox notUrgentNotImportant = createQuadrant(
                "ELIMINATE",
                "Not Urgent & Not Important",
                "neutral",
                notUrgentNotImportantContainer = new VBox(10),
                notUrgentNotImportantCount = new Label("0"));

        // Add to grid (2x2)
        add(urgentImportant, 0, 0);
        add(notUrgentImportant, 1, 0);
        add(urgentNotImportant, 0, 1);
        add(notUrgentNotImportant, 1, 1);

        // Make all quadrants equal size
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        getRowConstraints().addAll(row1, row2);
    }

    private VBox createQuadrant(String title, String subtitle, String colorStyle,
            VBox tasksContainer, Label countLabel) {
        VBox quadrant = new VBox();
        quadrant.getStyleClass().add(atlantafx.base.theme.Styles.ELEVATED_1);
        quadrant.setPadding(new Insets(0)); // Padding handled by children or CSS

        // Header
        VBox header = new VBox(5);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        // Apply semantic color style class to header
        header.getStyleClass().add("eisenhower-header-" + colorStyle);
        // Fallback style if CSS not updated yet, but prefer CSS classes
        String accentColor = switch (colorStyle) {
            case "danger" -> "-color-danger-fg";
            case "accent" -> "-color-accent-fg";
            case "warning" -> "-color-warning-fg";
            default -> "-color-fg-default";
        };

        header.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll(atlantafx.base.theme.Styles.TITLE_4);
        titleLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_MUTED);

        countLabel.getStyleClass().addAll(atlantafx.base.theme.Styles.TEXT_BOLD, atlantafx.base.theme.Styles.SUCCESS);
        // Custom badge style
        countLabel.setStyle(
                "-fx-background-color: -color-bg-default; " +
                        "-fx-text-fill: " + accentColor + "; " +
                        "-fx-border-color: " + accentColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 2 8 2 8; " +
                        "-fx-min-width: 30; -fx-alignment: center;");

        header.getChildren().addAll(titleLabel, subtitleLabel, countLabel);

        // Tasks container with scroll
        tasksContainer.setPadding(new Insets(15));
        tasksContainer.setSpacing(10);

        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        quadrant.getChildren().addAll(header, scrollPane);
        return quadrant;
    }

    public void loadTasks(List<Task> tasks) {
        // Clear all quadrants
        urgentImportantContainer.getChildren().clear();
        notUrgentImportantContainer.getChildren().clear();
        urgentNotImportantContainer.getChildren().clear();
        notUrgentNotImportantContainer.getChildren().clear();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime urgentThreshold = now.plusDays(URGENT_DAYS_THRESHOLD);

        // Filter out completed tasks
        List<Task> activeTasks = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .collect(Collectors.toList());

        int urgentImportantTotal = 0;
        int notUrgentImportantTotal = 0;
        int urgentNotImportantTotal = 0;
        int notUrgentNotImportantTotal = 0;

        for (Task task : activeTasks) {
            boolean isImportant = isImportant(task);
            boolean isUrgent = isUrgent(task, urgentThreshold);

            EisenhowerTaskCard card = new EisenhowerTaskCard(task, controller);

            if (isUrgent && isImportant) {
                urgentImportantContainer.getChildren().add(card);
                urgentImportantTotal++;
            } else if (!isUrgent && isImportant) {
                notUrgentImportantContainer.getChildren().add(card);
                notUrgentImportantTotal++;
            } else if (isUrgent && !isImportant) {
                urgentNotImportantContainer.getChildren().add(card);
                urgentNotImportantTotal++;
            } else {
                notUrgentNotImportantContainer.getChildren().add(card);
                notUrgentNotImportantTotal++;
            }
        }

        // Update counts
        urgentImportantCount.setText(String.valueOf(urgentImportantTotal));
        notUrgentImportantCount.setText(String.valueOf(notUrgentImportantTotal));
        urgentNotImportantCount.setText(String.valueOf(urgentNotImportantTotal));
        notUrgentNotImportantCount.setText(String.valueOf(notUrgentNotImportantTotal));

        // Show empty state if needed
        showEmptyStateIfNeeded(urgentImportantContainer, urgentImportantTotal, "No urgent important tasks");
        showEmptyStateIfNeeded(notUrgentImportantContainer, notUrgentImportantTotal,
                "No important tasks to schedule");
        showEmptyStateIfNeeded(urgentNotImportantContainer, urgentNotImportantTotal, "No tasks to delegate");
        showEmptyStateIfNeeded(notUrgentNotImportantContainer, notUrgentNotImportantTotal, "No tasks to eliminate");
    }

    private boolean isImportant(Task task) {
        // Important = High or Medium priority
        return task.getPriority() == Priority.HIGH || task.getPriority() == Priority.MEDIUM;
    }

    private boolean isUrgent(Task task, LocalDateTime urgentThreshold) {
        // Urgent = due date is within threshold OR overdue
        // No due date = Not Urgent (Schedule or Eliminate)
        if (task.getDueDate() == null) {
            return false;
        }
        return task.getDueDate().isBefore(urgentThreshold);
    }

    private void showEmptyStateIfNeeded(VBox container, int count, String message) {
        if (count == 0) {
            Label emptyLabel = new Label(message);
            emptyLabel.setFont(Font.font("Poppins", 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint; -fx-font-style: italic;");
            emptyLabel.setWrapText(true);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            container.getChildren().add(emptyLabel);
        }
    }

    // Task card for Eisenhower Matrix
    private static class EisenhowerTaskCard extends VBox {
        private final Task task;
        private final Button editBtn;
        private final Button completeBtn;

        public EisenhowerTaskCard(Task task, TasksController controller) {
            this.task = task;
            this.editBtn = new Button();
            this.completeBtn = new Button();

            setSpacing(8);
            setPadding(new Insets(12));
            // Use AtlantaFX interactive style for hover effects and borders
            getStyleClass().add(atlantafx.base.theme.Styles.INTERACTIVE);
            getStyleClass().add(atlantafx.base.theme.Styles.ELEVATED_1);
            setStyle("-fx-cursor: hand; -fx-background-radius: 8; -fx-border-radius: 8;");

            setOnMouseEntered(e -> {
                editBtn.setVisible(true);
                completeBtn.setVisible(true);
            });

            setOnMouseExited(e -> {
                editBtn.setVisible(false);
                completeBtn.setVisible(false);
            });

            setOnMouseClicked(e -> {
                if (e.getTarget() != editBtn && e.getTarget() != completeBtn
                        && !editBtn.contains(editBtn.sceneToLocal(e.getSceneX(), e.getSceneY()))
                        && !completeBtn.contains(completeBtn.sceneToLocal(e.getSceneX(), e.getSceneY()))) {
                    controller.editTask(task);
                }
            });

            // Top row: Priority + Operation + Edit + Complete
            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label priorityBadge = createPriorityBadge(task.getPriority());
            topRow.getChildren().add(priorityBadge);

            if (task.getOperationId() != null) {
                Optional<Operation> operation = controller.getOperationById(task.getOperationId());
                operation.ifPresent(op -> {
                    Label opBadge = new Label(op.getName());
                    opBadge.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_SMALL);
                    opBadge.setStyle(
                            "-fx-background-color: -color-accent-subtle; " +
                                    "-fx-text-fill: -color-accent-fg; " +
                                    "-fx-padding: 3 6 3 6; " +
                                    "-fx-background-radius: 8;");
                    topRow.getChildren().add(opBadge);
                });
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            topRow.getChildren().add(spacer);

            // Edit button
            FontIcon editIcon = new FontIcon(Feather.EDIT_2);
            editIcon.setIconSize(14);
            editBtn.setGraphic(editIcon);
            editBtn.getStyleClass().addAll(atlantafx.base.theme.Styles.BUTTON_ICON, atlantafx.base.theme.Styles.FLAT);
            editBtn.setVisible(false);
            editBtn.setOnAction(e -> {
                e.consume();
                controller.editTask(task);
            });
            topRow.getChildren().add(editBtn);

            // Complete button
            FontIcon completeIcon = new FontIcon(Feather.CHECK_CIRCLE);
            completeIcon.setIconSize(14);
            completeIcon.setIconColor(javafx.scene.paint.Color.web("#388E3C"));
            completeBtn.setGraphic(completeIcon);
            completeBtn.getStyleClass().addAll(atlantafx.base.theme.Styles.BUTTON_ICON,
                    atlantafx.base.theme.Styles.FLAT);
            completeBtn.setVisible(false);
            completeBtn.setOnAction(e -> {
                e.consume();
                controller.updateTaskStatus(task, TaskStatus.DONE);
            });
            topRow.getChildren().add(completeBtn);

            // Title
            Label titleLabel = new Label(task.getTitle());
            titleLabel.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_BOLD);
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            // Due date
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_LEFT);

            if (task.getDueDate() != null) {
                Label dueDateLabel = new Label("📅 " + formatDueDate(task.getDueDate()));
                dueDateLabel.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_SMALL);

                LocalDateTime now = LocalDateTime.now();
                if (task.getDueDate().isBefore(now)) {
                    dueDateLabel.getStyleClass().add(atlantafx.base.theme.Styles.DANGER);
                } else if (task.getDueDate().isBefore(now.plusDays(1))) {
                    dueDateLabel.getStyleClass().add(atlantafx.base.theme.Styles.WARNING);
                } else {
                    dueDateLabel.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_MUTED);
                }

                footer.getChildren().add(dueDateLabel);
            } else {
                Label noDueDateLabel = new Label("No due date");
                noDueDateLabel.getStyleClass().addAll(atlantafx.base.theme.Styles.TEXT_SMALL,
                        atlantafx.base.theme.Styles.TEXT_MUTED);
                footer.getChildren().add(noDueDateLabel);
            }

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, javafx.scene.layout.Priority.ALWAYS);
            footer.getChildren().add(footerSpacer);

            if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
                Label assigneeLabel = new Label(getInitials(task.getAssignee()));
                assigneeLabel.getStyleClass().addAll(atlantafx.base.theme.Styles.TEXT_SMALL,
                        atlantafx.base.theme.Styles.TEXT_BOLD);
                assigneeLabel.setStyle(
                        "-fx-background-color: -color-accent-emphasis; " +
                                "-fx-text-fill: -color-fg-emphasis; " +
                                "-fx-background-radius: 10; " +
                                "-fx-min-width: 20; " +
                                "-fx-min-height: 20; " +
                                "-fx-max-width: 20; " +
                                "-fx-max-height: 20; " +
                                "-fx-alignment: center;");
                footer.getChildren().add(assigneeLabel);
            }

            getChildren().addAll(topRow, titleLabel, footer);
        }

        private Label createPriorityBadge(Priority priority) {
            Label badge = new Label();
            badge.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_SMALL);
            badge.setStyle("-fx-padding: 3 8 3 8; -fx-background-radius: 10;");

            switch (priority) {
                case HIGH -> {
                    badge.setText("High");
                    badge.setStyle(badge.getStyle()
                            + "-fx-background-color: -color-danger-subtle; -fx-text-fill: -color-danger-fg;");
                }
                case MEDIUM -> {
                    badge.setText("Med");
                    badge.setStyle(badge.getStyle()
                            + "-fx-background-color: -color-warning-subtle; -fx-text-fill: -color-warning-fg;");
                }
                case LOW -> {
                    badge.setText("Low");
                    badge.setStyle(badge.getStyle()
                            + "-fx-background-color: -color-neutral-subtle; -fx-text-fill: -color-fg-default;");
                }
            }

            return badge;
        }

        private String formatDueDate(LocalDateTime dueDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            return formatter.format(dueDate);
        }

        private String getInitials(String name) {
            String[] parts = name.trim().split(" ");
            if (parts.length == 0)
                return "";
            if (parts.length == 1)
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}
