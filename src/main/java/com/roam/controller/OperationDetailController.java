package com.roam.controller;

import com.roam.model.Wiki;
import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.WikiRepository;
import com.roam.repository.OperationRepository;
import com.roam.repository.RegionRepository;
import com.roam.repository.TaskRepository;
import com.roam.util.DialogUtils;
import com.roam.view.components.TaskDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OperationDetailController {

    private static final Logger logger = LoggerFactory.getLogger(OperationDetailController.class);

    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final WikiRepository WikiRepository;
    private final RegionRepository regionRepository;
    private final CalendarEventRepository eventRepository;

    private Operation operation;
    private Runnable onDataChanged;

    public OperationDetailController(Operation operation) {
        this.operation = operation;
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.WikiRepository = new WikiRepository();
        this.regionRepository = new RegionRepository();
        this.eventRepository = new CalendarEventRepository();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    /**
     * Reload operation from database
     */
    public void refreshOperation() {
        operationRepository.findById(operation.getId()).ifPresent(op -> {
            this.operation = op;
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        });
    }

    /**
     * Update operation details
     */
    public void updateOperation(Operation updatedOperation) {
        try {
            operationRepository.save(updatedOperation);
            this.operation = updatedOperation;
            logger.debug("✓ Operation updated: {}", updatedOperation.getName());
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (Exception e) {
            logger.error("✗ Failed to update operation: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Update Error",
                    "Failed to update operation",
                    e.getMessage());
        }
    }

    // ==================== TASK OPERATIONS ====================

    /**
     * Load all tasks for this operation
     */
    public List<Task> loadTasks() {
        try {
            return taskRepository.findByOperationId(operation.getId());
        } catch (Exception e) {
            logger.error("✗ Failed to load tasks: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Load Error",
                    "Failed to load tasks",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Create new task
     */
    public void createTask(TaskStatus initialStatus) {
        Task task = new Task();
        task.setOperationId(operation.getId());
        task.setStatus(initialStatus != null ? initialStatus : TaskStatus.TODO);

        TaskDialog dialog = new TaskDialog(
                task,
                null,
                List.of(operation), // Only current operation
                regionRepository.findAll(),
                eventRepository.findAll(),
                WikiRepository.findAll());
        dialog.showAndWait().ifPresent(newTask -> {
            try {
                newTask.setOperationId(operation.getId());
                taskRepository.save(newTask);
                logger.debug("✓ Task created: {}", newTask.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                logger.error("✗ Failed to create task: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Create Error",
                        "Failed to create task",
                        e.getMessage());
            }
        });
    }

    /**
     * Edit existing task
     */
    public void editTask(Task task) {
        if (task == null)
            return;

        TaskDialog dialog = new TaskDialog(
                task,
                () -> deleteTask(task),
                List.of(operation),
                regionRepository.findAll(),
                eventRepository.findAll(),
                WikiRepository.findAll());
        dialog.showAndWait().ifPresent(updatedTask -> {
            try {
                taskRepository.save(updatedTask);
                logger.debug("✓ Task updated: {}", updatedTask.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                logger.error("✗ Failed to update task: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Update Error",
                        "Failed to update task",
                        e.getMessage());
            }
        });
    }

    /**
     * Delete task with confirmation
     */
    public void deleteTask(Task task) {
        if (task == null)
            return;

        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Task",
                "Are you sure you want to delete this task?",
                "Task: " + task.getTitle());

        if (confirmed) {
            try {
                taskRepository.delete(task);
                logger.debug("✓ Task deleted: {}", task.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                logger.error("✗ Failed to delete task: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Delete Error",
                        "Failed to delete task",
                        e.getMessage());
            }
        }
    }

    /**
     * Update task status (for drag and drop)
     */
    public void updateTaskStatus(Task task, TaskStatus newStatus) {
        if (task == null)
            return;

        try {
            task.setStatus(newStatus);
            taskRepository.save(task);
            logger.debug("✓ Task status updated: {} -> {}", task.getTitle(), newStatus);
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (Exception e) {
            logger.error("✗ Failed to update task status: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Update Error",
                    "Failed to update task status",
                    e.getMessage());
        }
    }

    // ==================== Wiki OPERATIONS ====================

    /**
     * Load all notes for this operation
     */
    public List<Wiki> loadNotes() {
        try {
            return WikiRepository.findByOperationId(operation.getId());
        } catch (Exception e) {
            logger.error("✗ Failed to load notes: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Load Error",
                    "Failed to load notes",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Create new Wiki
     */
    public Wiki createNote() {
        try {
            Wiki Wiki = new Wiki("Untitled Wiki", operation.getId());
            Wiki = WikiRepository.save(Wiki);
            logger.debug("✓ Wiki created: {}", Wiki.getTitle());
            if (onDataChanged != null) {
                onDataChanged.run();
            }
            return Wiki;
        } catch (Exception e) {
            logger.error("✗ Failed to create Wiki: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Create Error",
                    "Failed to create Wiki",
                    e.getMessage());
            return null;
        }
    }

    /**
     * Save Wiki
     */
    public void saveNote(Wiki Wiki) {
        if (Wiki == null)
            return;

        try {
            WikiRepository.save(Wiki);
            logger.debug("✓ Wiki saved: {}", Wiki.getTitle());
        } catch (Exception e) {
            logger.error("✗ Failed to save Wiki: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Save Error",
                    "Failed to save Wiki",
                    e.getMessage());
        }
    }

    /**
     * Delete Wiki with confirmation
     */
    public void deleteNote(Wiki Wiki) {
        if (Wiki == null)
            return;

        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Wiki",
                "Are you sure you want to delete this Wiki?",
                "Wiki: " + Wiki.getTitle());

        if (confirmed) {
            try {
                WikiRepository.delete(Wiki);
                logger.debug("✓ Wiki deleted: {}", Wiki.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                logger.error("✗ Failed to delete Wiki: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Delete Error",
                        "Failed to delete Wiki",
                        e.getMessage());
            }
        }
    }

    /**
     * Get all regions
     */
    public List<Region> getAllRegions() {
        return regionRepository.findAll();
    }
}
