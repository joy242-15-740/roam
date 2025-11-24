package com.roam.service;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Operation business logic and transaction management.
 * Provides a clean abstraction layer between controllers and repositories.
 */
public interface OperationService {

    /**
     * Create a new operation.
     * 
     * @param operation Operation to create
     * @return Created operation with generated ID
     * @throws IllegalArgumentException if operation is null or invalid
     */
    Operation createOperation(Operation operation);

    /**
     * Update an existing operation.
     * 
     * @param operation Operation to update
     * @return Updated operation
     * @throws IllegalArgumentException if operation is null or ID is null
     */
    Operation updateOperation(Operation operation);

    /**
     * Delete an operation by ID.
     * Also deletes associated tasks and notes.
     * 
     * @param id Operation ID
     * @throws IllegalArgumentException if id is null
     */
    void deleteOperation(Long id);

    /**
     * Find operation by ID.
     * 
     * @param id Operation ID
     * @return Optional containing operation if found
     */
    Optional<Operation> findById(Long id);

    /**
     * Find all operations ordered by creation date (newest first).
     * 
     * @return List of all operations
     */
    List<Operation> findAll();

    /**
     * Find operations by status.
     * 
     * @param status Operation status
     * @return List of operations with given status
     */
    List<Operation> findByStatus(OperationStatus status);

    /**
     * Find operations by priority.
     * 
     * @param priority Operation priority
     * @return List of operations with given priority
     */
    List<Operation> findByPriority(Priority priority);

    /**
     * Find operations with due date before given date.
     * 
     * @param date Date threshold
     * @return List of operations due before date
     */
    List<Operation> findDueBefore(LocalDate date);

    /**
     * Find recently updated operations (last 5).
     * Useful for "Recent Activity" features.
     * 
     * @return List of 5 most recently updated operations
     */
    List<Operation> findRecentlyUpdated();

    /**
     * Count all operations.
     * 
     * @return Total number of operations
     */
    long count();

    /**
     * Count operations by status.
     * 
     * @param status Operation status
     * @return Number of operations with given status
     */
    long countByStatus(OperationStatus status);

    /**
     * Index operation in search service.
     * Called automatically after create/update.
     * 
     * @param operation Operation to index
     */
    void indexOperation(Operation operation);
}
