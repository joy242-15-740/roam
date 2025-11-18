package com.roam.repository;

import com.roam.model.CalendarEvent;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CalendarEventRepository {

    public CalendarEvent save(CalendarEvent event) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (event.getId() == null) {
                em.persist(event);
                System.out.println("✓ Calendar event created: " + event.getTitle());
            } else {
                event = em.merge(event);
                System.out.println("✓ Calendar event updated: " + event.getTitle());
            }

            tx.commit();
            return event;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to save calendar event: " + e.getMessage());
            throw new RuntimeException("Failed to save calendar event", e);
        } finally {
            em.close();
        }
    }

    public Optional<CalendarEvent> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            CalendarEvent event = em.find(CalendarEvent.class, id);
            return Optional.ofNullable(event);
        } finally {
            em.close();
        }
    }

    public List<CalendarEvent> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CalendarEvent> findByDateRange(LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.startDateTime >= :start AND e.startDateTime < :end ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CalendarEvent> findByCalendarSourceId(Long sourceId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.calendarSourceId = :sourceId ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("sourceId", sourceId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<CalendarEvent> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.operationId = :operationId ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("operationId", operationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<CalendarEvent> findByTaskId(Long taskId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.taskId = :taskId",
                    CalendarEvent.class);
            query.setParameter("taskId", taskId);
            List<CalendarEvent> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<CalendarEvent> findRecurringEvents() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.recurrenceRule IS NOT NULL AND e.isRecurringInstance = false",
                    CalendarEvent.class);
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

            CalendarEvent event = em.find(CalendarEvent.class, id);
            if (event != null) {
                em.remove(event);
                System.out.println("✓ Calendar event deleted: " + event.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete calendar event: " + e.getMessage());
            throw new RuntimeException("Failed to delete calendar event", e);
        } finally {
            em.close();
        }
    }

    public void deleteRecurringSeries(Long parentEventId) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // Delete parent event
            CalendarEvent parent = em.find(CalendarEvent.class, parentEventId);
            if (parent != null) {
                em.remove(parent);
            }

            // Delete all instances
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.parentEventId = :parentId",
                    CalendarEvent.class);
            query.setParameter("parentId", parentEventId);
            List<CalendarEvent> instances = query.getResultList();

            for (CalendarEvent instance : instances) {
                em.remove(instance);
            }

            tx.commit();
            System.out.println("✓ Recurring series deleted (parent and " + instances.size() + " instances)");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete recurring series: " + e.getMessage());
            throw new RuntimeException("Failed to delete recurring series", e);
        } finally {
            em.close();
        }
    }
}
