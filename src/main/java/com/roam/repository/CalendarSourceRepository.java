package com.roam.repository;

import com.roam.model.CalendarSource;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class CalendarSourceRepository {

    public CalendarSource save(CalendarSource source) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (source.getId() == null) {
                em.persist(source);
                System.out.println("✓ Calendar source created: " + source.getName());
            } else {
                source = em.merge(source);
                System.out.println("✓ Calendar source updated: " + source.getName());
            }

            tx.commit();
            return source;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to save calendar source: " + e.getMessage());
            throw new RuntimeException("Failed to save calendar source", e);
        } finally {
            em.close();
        }
    }

    public Optional<CalendarSource> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            CalendarSource source = em.find(CalendarSource.class, id);
            return Optional.ofNullable(source);
        } finally {
            em.close();
        }
    }

    public List<CalendarSource> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c ORDER BY c.createdAt ASC",
                    CalendarSource.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CalendarSource> findVisible() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c WHERE c.isVisible = true ORDER BY c.createdAt ASC",
                    CalendarSource.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<CalendarSource> findDefault() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c WHERE c.isDefault = true",
                    CalendarSource.class);
            List<CalendarSource> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
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

            CalendarSource source = em.find(CalendarSource.class, id);
            if (source != null) {
                em.remove(source);
                System.out.println("✓ Calendar source deleted: " + source.getName());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete calendar source: " + e.getMessage());
            throw new RuntimeException("Failed to delete calendar source", e);
        } finally {
            em.close();
        }
    }
}
