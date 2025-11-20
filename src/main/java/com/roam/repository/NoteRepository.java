package com.roam.repository;

import com.roam.model.Note;
import com.roam.model.Tag;
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

    /**
     * Find all notes
     */
    public List<Note> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> query = em.createQuery(
                    "SELECT n FROM Note n ORDER BY n.updatedAt DESC",
                    Note.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find all favorite notes
     */
    public List<Note> findFavorites() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> query = em.createQuery(
                    "SELECT n FROM Note n WHERE n.isFavorite = true ORDER BY n.updatedAt DESC",
                    Note.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find recent notes
     */
    public List<Note> findRecent(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> query = em.createQuery(
                    "SELECT n FROM Note n ORDER BY n.updatedAt DESC",
                    Note.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find notes by tag
     */
    public List<Note> findByTag(Tag tag) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> query = em.createQuery(
                    "SELECT n FROM Note n JOIN n.tags t WHERE t.id = :tagId ORDER BY n.updatedAt DESC",
                    Note.class);
            query.setParameter("tagId", tag.getId());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Search notes by query (title and content)
     */
    public List<Note> searchFullText(String query) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Note> q = em.createQuery(
                    "SELECT n FROM Note n WHERE LOWER(n.title) LIKE LOWER(:query) OR LOWER(n.content) LIKE LOWER(:query) ORDER BY n.updatedAt DESC",
                    Note.class);
            q.setParameter("query", "%" + query + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Get total word count across all notes
     */
    public long getTotalWordCount() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT SUM(n.wordCount) FROM Note n",
                    Long.class);
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } finally {
            em.close();
        }
    }
}
