package com.roam.repository;

import com.roam.model.JournalTemplate;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class JournalTemplateRepository {

    public JournalTemplate save(JournalTemplate template) {
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
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<JournalTemplate> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(JournalTemplate.class, id));
        } finally {
            em.close();
        }
    }

    public List<JournalTemplate> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalTemplate> query = em.createQuery("SELECT t FROM JournalTemplate t ORDER BY t.name ASC",
                    JournalTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void delete(JournalTemplate template) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            JournalTemplate managed = em.find(JournalTemplate.class, template.getId());
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
