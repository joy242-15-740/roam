package com.roam.controller;

import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.service.SearchService;
import com.roam.repository.OperationRepository;
import com.roam.repository.RegionRepository;
import com.roam.util.DialogUtils;
import com.roam.view.components.OperationDialog;
import com.roam.view.components.OperationTableView;

import java.util.List;
import java.util.Optional;

public class OperationsController {

    private final OperationRepository repository;
    private final RegionRepository regionRepository;
    private OperationTableView tableView;
    private Runnable onDataChanged;

    public OperationsController() {
        this.repository = new OperationRepository();
        this.regionRepository = new RegionRepository();
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
        List<Region> regions = regionRepository.findAll();
        OperationDialog dialog = new OperationDialog(null, regions);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(operation -> {
            try {
                Operation savedOperation = repository.save(operation);
                indexOperation(savedOperation);
                System.out.println("✓ Operation created: " + savedOperation.getName());
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

        List<Region> regions = regionRepository.findAll();
        OperationDialog dialog = new OperationDialog(operation, regions);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(updatedOp -> {
            try {
                Operation savedOperation = repository.save(updatedOp);
                indexOperation(savedOperation);
                System.out.println("✓ Operation updated: " + savedOperation.getName());
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
                try {
                    SearchService.getInstance().deleteDocument(operation.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    private void indexOperation(Operation operation) {
        try {
            SearchService.getInstance().indexOperation(
                    operation.getId(),
                    operation.getName(),
                    operation.getPurpose(),
                    operation.getOutcome(),
                    operation.getStatus() != null ? operation.getStatus().toString() : null,
                    operation.getPriority() != null ? operation.getPriority().toString() : null);
        } catch (Exception e) {
            System.err.println("Failed to index operation: " + e.getMessage());
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
