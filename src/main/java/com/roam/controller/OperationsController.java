package com.roam.controller;

import com.roam.model.Operation;
import com.roam.repository.OperationRepository;
import com.roam.util.DialogUtils;
import com.roam.view.components.OperationDialog;
import com.roam.view.components.OperationTableView;

import java.util.List;
import java.util.Optional;

public class OperationsController {

    private final OperationRepository repository;
    private OperationTableView tableView;
    private Runnable onDataChanged;

    public OperationsController() {
        this.repository = new OperationRepository();
    }

    public void setTableView(OperationTableView tableView) {
        this.tableView = tableView;
    }

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    /**
     * Load all operations from database
     */
    public List<Operation> loadOperations() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            System.err.println("Failed to load operations: " + e.getMessage());
            DialogUtils.showError(
                    "Database Error",
                    "Failed to load operations",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Create new operation
     */
    public void createOperation() {
        OperationDialog dialog = new OperationDialog(null);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(operation -> {
            try {
                repository.save(operation);
                System.out.println("✓ Operation created: " + operation.getName());
                refreshTable();
                // Optional: DialogUtils.showSuccess("Operation created successfully!");
            } catch (Exception e) {
                System.err.println("✗ Failed to create operation: " + e.getMessage());
                DialogUtils.showError(
                        "Save Error",
                        "Failed to create operation",
                        e.getMessage());
            }
        });
    }

    /**
     * Edit existing operation
     */
    public void editOperation(Operation operation) {
        if (operation == null)
            return;

        OperationDialog dialog = new OperationDialog(operation);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(updatedOp -> {
            try {
                repository.save(updatedOp);
                System.out.println("✓ Operation updated: " + updatedOp.getName());
                refreshTable();
                // Optional: DialogUtils.showSuccess("Operation updated successfully!");
            } catch (Exception e) {
                System.err.println("✗ Failed to update operation: " + e.getMessage());
                DialogUtils.showError(
                        "Update Error",
                        "Failed to update operation",
                        e.getMessage());
            }
        });
    }

    /**
     * Delete operation with confirmation
     */
    public void deleteOperation(Operation operation) {
        if (operation == null)
            return;

        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Operation",
                "Are you sure you want to delete this operation?",
                "Operation: " + operation.getName() + "\n\nThis action cannot be undone.");

        if (confirmed) {
            try {
                repository.delete(operation);
                System.out.println("✓ Operation deleted: " + operation.getName());
                refreshTable();
                // Optional: DialogUtils.showSuccess("Operation deleted successfully!");
            } catch (Exception e) {
                System.err.println("✗ Failed to delete operation: " + e.getMessage());
                DialogUtils.showError(
                        "Delete Error",
                        "Failed to delete operation",
                        e.getMessage());
            }
        }
    }

    /**
     * Refresh table data
     */
    public void refreshTable() {
        if (tableView != null) {
            List<Operation> operations = loadOperations();
            tableView.getItems().setAll(operations);
        }

        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
