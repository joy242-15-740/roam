package com.roam.repository;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

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
}
