package com.roam.repository;

import com.roam.model.JournalEntry;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JournalEntryRepository {

    public JournalEntry save(JournalEntry entry) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            if (entry.getId() == null) {
                em.persist(entry);
                em.flush(); // Ensure ID is generated
            } else {
                entry = em.merge(entry);
            }
            tx.commit();
            return entry;
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<JournalEntry> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(JournalEntry.class, id));
        } finally {
            em.close();
        }
    }

    public List<JournalEntry> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery("SELECT j FROM JournalEntry j ORDER BY j.date DESC",
                    JournalEntry.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<JournalEntry> findByDate(LocalDate date) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery("SELECT j FROM JournalEntry j WHERE j.date = :date",
                    JournalEntry.class);
            query.setParameter("date", date);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    public void delete(JournalEntry entry) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            JournalEntry managed = em.find(JournalEntry.class, entry.getId());
            if (managed != null) {
                em.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
