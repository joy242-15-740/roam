package com.roam.repository;

import com.roam.model.WikiFileAttachment;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class WikiFileAttachmentRepository {

    public WikiFileAttachment save(WikiFileAttachment attachment) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (attachment.getId() == null) {
                em.persist(attachment);
            } else {
                attachment = em.merge(attachment);
            }
            em.getTransaction().commit();
            return attachment;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to save WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    public Optional<WikiFileAttachment> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            WikiFileAttachment attachment = em.find(WikiFileAttachment.class, id);
            return Optional.ofNullable(attachment);
        } finally {
            em.close();
        }
    }

    public List<WikiFileAttachment> findByWikiId(Long wikiId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiFileAttachment> query = em.createQuery(
                    "SELECT w FROM WikiFileAttachment w WHERE w.wikiId = :wikiId ORDER BY w.createdAt DESC",
                    WikiFileAttachment.class);
            query.setParameter("wikiId", wikiId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(WikiFileAttachment attachment) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            WikiFileAttachment managed = em.merge(attachment);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    public void deleteById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            WikiFileAttachment attachment = em.find(WikiFileAttachment.class, id);
            if (attachment != null) {
                em.remove(attachment);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    public void deleteByWikiId(Long wikiId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM WikiFileAttachment w WHERE w.wikiId = :wikiId")
                    .setParameter("wikiId", wikiId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachments for wiki", e);
        } finally {
            em.close();
        }
    }
}
