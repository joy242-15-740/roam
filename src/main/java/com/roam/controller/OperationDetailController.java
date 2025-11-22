package com.roam.controller;

import com.roam.model.Note;
import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.NoteRepository;
import com.roam.repository.OperationRepository;
import com.roam.repository.RegionRepository;
import com.roam.repository.TaskRepository;
import com.roam.util.DialogUtils;
import com.roam.view.components.TaskDialog;

import java.util.List;

public class OperationDetailController {

    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final RegionRepository regionRepository;
    private final CalendarEventRepository eventRepository;

    private Operation operation;
    private Runnable onDataChanged;

    public OperationDetailController(Operation operation) {
        this.operation = operation;
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.noteRepository = new NoteRepository();
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
            System.out.println("✓ Operation updated: " + updatedOperation.getName());
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to update operation: " + e.getMessage());
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
            System.err.println("✗ Failed to load tasks: " + e.getMessage());
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
                noteRepository.findAll());
        dialog.showAndWait().ifPresent(newTask -> {
            try {
                newTask.setOperationId(operation.getId());
                taskRepository.save(newTask);
                System.out.println("✓ Task created: " + newTask.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to create task: " + e.getMessage());
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
                noteRepository.findAll());
        dialog.showAndWait().ifPresent(updatedTask -> {
            try {
                taskRepository.save(updatedTask);
                System.out.println("✓ Task updated: " + updatedTask.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to update task: " + e.getMessage());
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
                System.out.println("✓ Task deleted: " + task.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to delete task: " + e.getMessage());
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
            System.out.println("✓ Task status updated: " + task.getTitle() + " -> " + newStatus);
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (Exception e) {
            System.err.println("✗ Failed to update task status: " + e.getMessage());
            DialogUtils.showError(
                    "Update Error",
                    "Failed to update task status",
                    e.getMessage());
        }
    }

    // ==================== NOTE OPERATIONS ====================

    /**
     * Load all notes for this operation
     */
    public List<Note> loadNotes() {
        try {
            return noteRepository.findByOperationId(operation.getId());
        } catch (Exception e) {
            System.err.println("✗ Failed to load notes: " + e.getMessage());
            DialogUtils.showError(
                    "Load Error",
                    "Failed to load notes",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Create new note
     */
    public Note createNote() {
        try {
            Note note = new Note("Untitled Note", operation.getId());
            note = noteRepository.save(note);
            System.out.println("✓ Note created: " + note.getTitle());
            if (onDataChanged != null) {
                onDataChanged.run();
            }
            return note;
        } catch (Exception e) {
            System.err.println("✗ Failed to create note: " + e.getMessage());
            DialogUtils.showError(
                    "Create Error",
                    "Failed to create note",
                    e.getMessage());
            return null;
        }
    }

    /**
     * Save note
     */
    public void saveNote(Note note) {
        if (note == null)
            return;

        try {
            noteRepository.save(note);
            System.out.println("✓ Note saved: " + note.getTitle());
        } catch (Exception e) {
            System.err.println("✗ Failed to save note: " + e.getMessage());
            DialogUtils.showError(
                    "Save Error",
                    "Failed to save note",
                    e.getMessage());
        }
    }

    /**
     * Delete note with confirmation
     */
    public void deleteNote(Note note) {
        if (note == null)
            return;

        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Note",
                "Are you sure you want to delete this note?",
                "Note: " + note.getTitle());

        if (confirmed) {
            try {
                noteRepository.delete(note);
                System.out.println("✓ Note deleted: " + note.getTitle());
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                System.err.println("✗ Failed to delete note: " + e.getMessage());
                DialogUtils.showError(
                        "Delete Error",
                        "Failed to delete note",
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
