package com.roam.repository;

import com.roam.model.WikiTemplate;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class WikiTemplateRepository {

    private static final Logger logger = LoggerFactory.getLogger(WikiTemplateRepository.class);

    public WikiTemplate save(WikiTemplate template) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (template.getId() == null) {
                em.persist(template);
            } else {
                template = em.merge(template);
            }

            tx.commit();
            return template;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save template", e);
        } finally {
            em.close();
        }
    }

    public Optional<WikiTemplate> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            WikiTemplate template = em.find(WikiTemplate.class, id);
            return Optional.ofNullable(template);
        } finally {
            em.close();
        }
    }

    public List<WikiTemplate> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t ORDER BY t.isDefault DESC, t.name ASC",
                    WikiTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<WikiTemplate> findDefaults() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t WHERE t.isDefault = true ORDER BY t.name",
                    WikiTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<WikiTemplate> findCustom() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t WHERE t.isDefault = false ORDER BY t.name",
                    WikiTemplate.class);
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

            WikiTemplate template = em.find(WikiTemplate.class, id);
            if (template != null) {
                em.remove(template);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete template", e);
        } finally {
            em.close();
        }
    }
}
