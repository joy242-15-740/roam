package com.roam.repository;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class OperationRepository {

    /**
     * Save (create or update) an operation
     */
    public Operation save(Operation operation) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (operation.getId() == null) {
                // New operation - persist
                em.persist(operation);
                System.out.println("✓ Operation created: " + operation.getName());
            } else {
                // Existing operation - merge
                operation = em.merge(operation);
                System.out.println("✓ Operation updated: " + operation.getName());
            }

            tx.commit();
            return operation;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to save operation: " + e.getMessage());
            throw new RuntimeException("Failed to save operation", e);
        } finally {
            em.close();
        }
    }

    /**
     * Find operation by ID
     */
    public Optional<Operation> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Operation operation = em.find(Operation.class, id);
            return Optional.ofNullable(operation);
        } finally {
            em.close();
        }
    }

    /**
     * Find all operations
     */
    public List<Operation> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o ORDER BY o.createdAt DESC",
                    Operation.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find operations by status
     */
    public List<Operation> findByStatus(OperationStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o WHERE o.status = :status ORDER BY o.createdAt DESC",
                    Operation.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find operations by priority
     */
    public List<Operation> findByPriority(Priority priority) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o WHERE o.priority = :priority ORDER BY o.createdAt DESC",
                    Operation.class);
            query.setParameter("priority", priority);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Delete operation
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Operation operation = em.find(Operation.class, id);
            if (operation != null) {
                em.remove(operation);
                System.out.println("✓ Operation deleted: " + operation.getName());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete operation: " + e.getMessage());
            throw new RuntimeException("Failed to delete operation", e);
        } finally {
            em.close();
        }
    }

    /**
     * Delete operation entity
     */
    public void delete(Operation operation) {
        if (operation != null && operation.getId() != null) {
            delete(operation.getId());
        }
    }

    /**
     * Count total operations
     */
    public long count() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(o) FROM Operation o",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
