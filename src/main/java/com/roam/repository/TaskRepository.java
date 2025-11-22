package com.roam.repository;

import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskFilter;
import com.roam.model.TaskStatus;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskRepository {

    public Task save(Task task) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (task.getId() == null) {
                em.persist(task);
                System.out.println("✓ Task created: " + task.getTitle());
            } else {
                task = em.merge(task);
                System.out.println("✓ Task updated: " + task.getTitle());
            }

            tx.commit();
            return task;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to save task: " + e.getMessage());
            throw new RuntimeException("Failed to save task", e);
        } finally {
            em.close();
        }
    }

    public Optional<Task> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Task task = em.find(Task.class, id);
            return Optional.ofNullable(task);
        } finally {
            em.close();
        }
    }

    public List<Task> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.operationId = :operationId ORDER BY t.position ASC, t.createdAt DESC",
                    Task.class);
            query.setParameter("operationId", operationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Task> findByOperationIdAndStatus(Long operationId, TaskStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.operationId = :operationId AND t.status = :status ORDER BY t.position ASC, t.createdAt DESC",
                    Task.class);
            query.setParameter("operationId", operationId);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task task = em.find(Task.class, id);
            if (task != null) {
                em.remove(task);
                System.out.println("✓ Task deleted: " + task.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete task: " + e.getMessage());
            throw new RuntimeException("Failed to delete task", e);
        } finally {
            em.close();
        }
    }

    public void delete(Task task) {
        if (task != null && task.getId() != null) {
            delete(task.getId());
        }
    }

    public void updatePosition(Long id, Integer position) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task task = em.find(Task.class, id);
            if (task != null) {
                task.setPosition(position);
                em.merge(task);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to update task position: " + e.getMessage());
            throw new RuntimeException("Failed to update task position", e);
        } finally {
            em.close();
        }
    }

    /**
     * Find all tasks across all operations
     */
    public List<Task> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t ORDER BY t.createdAt DESC",
                    Task.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find tasks with filters applied
     */
    public List<Task> findWithFilters(TaskFilter filter) {
        List<Task> allTasks = findAll();

        // Apply filters in Java (simplified - in production use JPQL WHERE clauses)
        return allTasks.stream()
                .filter(task -> {
                    // Operation filter
                    if (!filter.getOperationIds().isEmpty()) {
                        if (!filter.getOperationIds().contains(task.getOperationId())) {
                            return false;
                        }
                    }

                    // Status filter
                    if (!filter.getStatuses().isEmpty()) {
                        if (!filter.getStatuses().contains(task.getStatus())) {
                            return false;
                        }
                    }

                    // Priority filter
                    if (!filter.getPriorities().isEmpty()) {
                        if (!filter.getPriorities().contains(task.getPriority())) {
                            return false;
                        }
                    }

                    // Assignee filter
                    if (!filter.getAssignees().isEmpty()) {
                        String assignee = task.getAssignee();
                        if (assignee == null || !filter.getAssignees().contains(assignee)) {
                            return false;
                        }
                    }

                    // Search query
                    if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                        String query = filter.getSearchQuery().toLowerCase();
                        String title = task.getTitle() != null ? task.getTitle().toLowerCase() : "";
                        String desc = task.getDescription() != null ? task.getDescription().toLowerCase() : "";
                        if (!title.contains(query) && !desc.contains(query)) {
                            return false;
                        }
                    }

                    // Due date filter
                    if (filter.getDueDateFilter() != TaskFilter.DueDateFilter.ANY) {
                        LocalDateTime dueDate = task.getDueDate();
                        LocalDateTime now = LocalDateTime.now();

                        switch (filter.getDueDateFilter()) {
                            case OVERDUE:
                                if (dueDate == null || !dueDate.isBefore(now)) {
                                    return false;
                                }
                                break;
                            case TODAY:
                                if (dueDate == null || !dueDate.toLocalDate().equals(LocalDate.now())) {
                                    return false;
                                }
                                break;
                            case TOMORROW:
                                if (dueDate == null || !dueDate.toLocalDate().equals(LocalDate.now().plusDays(1))) {
                                    return false;
                                }
                                break;
                            case THIS_WEEK:
                                if (dueDate == null || dueDate.isAfter(now.plusDays(7))) {
                                    return false;
                                }
                                break;
                            case THIS_MONTH:
                                if (dueDate == null || dueDate.isAfter(now.plusDays(30))) {
                                    return false;
                                }
                                break;
                            case NO_DUE_DATE:
                                if (dueDate != null) {
                                    return false;
                                }
                                break;
                            case HAS_DUE_DATE:
                                if (dueDate == null) {
                                    return false;
                                }
                                break;
                        }
                    }

                    // Show completed filter
                    if (!filter.getShowCompleted() && task.getStatus() == TaskStatus.DONE) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Count tasks by status
     */
    public long countByStatus(TaskStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.status = :status",
                    Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Count all tasks
     */
    public long countAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Get all unique assignees
     */
    public List<String> getAllAssignees() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                    "SELECT DISTINCT t.assignee FROM Task t WHERE t.assignee IS NOT NULL ORDER BY t.assignee",
                    String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Count high priority tasks (not done)
     */
    public long countHighPriority() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.priority = :priority AND t.status != :status",
                    Long.class);
            query.setParameter("priority", Priority.HIGH);
            query.setParameter("status", TaskStatus.DONE);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Count overdue tasks (not done)
     */
    public long countOverdue() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now AND t.status != :status",
                    Long.class);
            query.setParameter("now", LocalDateTime.now());
            query.setParameter("status", TaskStatus.DONE);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Batch update multiple tasks
     */
    public int batchUpdateStatus(List<Long> taskIds, TaskStatus newStatus) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setStatus(newStatus);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            System.out.println("✓ Batch updated " + count + " tasks to status: " + newStatus);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to batch update: " + e.getMessage());
            throw new RuntimeException("Failed to batch update", e);
        } finally {
            em.close();
        }
    }

    /**
     * Batch delete multiple tasks
     */
    public int batchDelete(List<Long> taskIds) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    em.remove(task);
                    count++;
                }
            }

            tx.commit();
            System.out.println("✓ Batch deleted " + count + " tasks");
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to batch delete: " + e.getMessage());
            throw new RuntimeException("Failed to batch delete", e);
        } finally {
            em.close();
        }
    }

    /**
     * Batch update priority for multiple tasks
     */
    public int batchUpdatePriority(List<Long> taskIds, Priority newPriority) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setPriority(newPriority);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            System.out.println("✓ Batch updated priority for " + count + " tasks to: " + newPriority);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to batch update priority: " + e.getMessage());
            throw new RuntimeException("Failed to batch update priority", e);
        } finally {
            em.close();
        }
    }

    /**
     * Batch assign tasks to an assignee
     */
    public int batchAssign(List<Long> taskIds, String assignee) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setAssignee(assignee);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            String action = assignee == null ? "Unassigned" : "Assigned to " + assignee;
            System.out.println("✓ " + action + " for " + count + " tasks");
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to batch assign: " + e.getMessage());
            throw new RuntimeException("Failed to batch assign", e);
        } finally {
            em.close();
        }
    }

    /**
     * Batch set due date for multiple tasks
     */
    public int batchSetDueDate(List<Long> taskIds, LocalDateTime dueDate) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setDueDate(dueDate);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            String action = dueDate == null ? "Cleared due date" : "Set due date";
            System.out.println("✓ " + action + " for " + count + " tasks");
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to batch set due date: " + e.getMessage());
            throw new RuntimeException("Failed to batch set due date", e);
        } finally {
            em.close();
        }
    }
}
