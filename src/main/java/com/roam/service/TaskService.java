package com.roam.service;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Task business logic and transaction management.
 */
public interface TaskService {

    Task createTask(Task task);

    Task updateTask(Task task);

    void deleteTask(Long id);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    List<Task> findByOperationId(Long operationId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByPriority(Priority priority);

    List<Task> findDueBefore(LocalDate date);

    List<Task> findOverdue();

    long count();

    long countByStatus(TaskStatus status);

    Task updateStatus(Long id, TaskStatus newStatus);

    void indexTask(Task task);
}
