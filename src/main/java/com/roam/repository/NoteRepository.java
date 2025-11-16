package com.roam.repository;

import com.roam.model.Note;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class NoteRepository {

    public Note save(Note note) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (note.getId() == null) {
                em.persist(note);
                System.out.println("✓ Note created: " + note.getTitle());
            } else {
                note = em.merge(note);
                System.out.println("✓ Note updated: " + note.getTitle());
            }

            tx.commit();
            return note;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to save note: " + e.getMessage());
            throw new RuntimeException("Failed to save note", e);
        } finally {
            em.close();
        }
    }

    public Optional<Note> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Note note = em.find(Note.class, id);
            return Optional.ofNullable(note);
        } finally {
            em.close();
        }
    }

    public List<Note> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> query = em.createQuery(
                    "SELECT n FROM Note n WHERE n.operationId = :operationId ORDER BY n.updatedAt DESC",
                    Note.class);
            query.setParameter("operationId", operationId);
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

            Note note = em.find(Note.class, id);
            if (note != null) {
                em.remove(note);
                System.out.println("✓ Note deleted: " + note.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("✗ Failed to delete note: " + e.getMessage());
            throw new RuntimeException("Failed to delete note", e);
        } finally {
            em.close();
        }
    }

    public void delete(Note note) {
        if (note != null && note.getId() != null) {
            delete(note.getId());
        }
    }
}
