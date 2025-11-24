package com.roam.repository;

import com.roam.model.Wiki;
import com.roam.service.ValidationService;
import com.roam.util.HibernateUtil;
import com.roam.util.InputSanitizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class WikiRepository {

    private static final Logger logger = LoggerFactory.getLogger(WikiRepository.class);

    private final ValidationService validationService = ValidationService.getInstance();

    public Wiki save(Wiki Wiki) {
        // Validate entity before persisting
        validationService.validate(Wiki);

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (Wiki.getId() == null) {
                em.persist(Wiki);
                em.flush(); // Ensure ID is generated
                logger.debug("✓ Wiki created: {}", Wiki.getTitle());
            } else {
                Wiki = em.merge(Wiki);
                logger.debug("✓ Wiki updated: {}", Wiki.getTitle());
            }

            tx.commit();
            return Wiki;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save Wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save Wiki", e);
        } finally {
            em.close();
        }
    }

    public Optional<Wiki> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Wiki Wiki = em.find(Wiki.class, id);
            return Optional.ofNullable(Wiki);
        } finally {
            em.close();
        }
    }

    public List<Wiki> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n WHERE n.operationId = :operationId ORDER BY n.updatedAt DESC",
                    Wiki.class);
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

            Wiki Wiki = em.find(Wiki.class, id);
            if (Wiki != null) {
                em.remove(Wiki);
                logger.debug("✓ Wiki deleted: {}", Wiki.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete Wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete Wiki", e);
        } finally {
            em.close();
        }
    }

    public void delete(Wiki Wiki) {
        if (Wiki != null && Wiki.getId() != null) {
            delete(Wiki.getId());
        }
    }

    /**
     * Find all notes
     */
    public List<Wiki> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n ORDER BY n.updatedAt DESC",
                    Wiki.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find all favorite notes
     */
    public List<Wiki> findFavorites() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n WHERE n.isFavorite = true ORDER BY n.updatedAt DESC",
                    Wiki.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Find recent notes
     */
    public List<Wiki> findRecent(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n ORDER BY n.updatedAt DESC",
                    Wiki.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Search notes by query (title and content)
     */
    public List<Wiki> searchFullText(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Sanitize input to prevent JPQL injection
        String sanitizedQuery = InputSanitizer.sanitizeForJPQL(query);

        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> q = em.createQuery(
                    "SELECT n FROM Wiki n WHERE LOWER(n.title) LIKE LOWER(:query) OR LOWER(n.content) LIKE LOWER(:query) ORDER BY n.updatedAt DESC",
                    Wiki.class);
            q.setParameter("query", "%" + sanitizedQuery + "%");
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
                    "SELECT SUM(n.wordCount) FROM Wiki n",
                    Long.class);
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } finally {
            em.close();
        }
    }
}
