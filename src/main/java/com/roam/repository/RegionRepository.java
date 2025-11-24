package com.roam.repository;

import com.roam.model.Region;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class RegionRepository {

    private static final Logger logger = LoggerFactory.getLogger(RegionRepository.class);

    public Region save(Region region) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (region.getId() == null) {
                em.persist(region);
            } else {
                region = em.merge(region);
            }

            tx.commit();
            return region;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save region: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save region", e);
        } finally {
            em.close();
        }
    }

    public Optional<Region> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Region region = em.find(Region.class, id);
            return Optional.ofNullable(region);
        } finally {
            em.close();
        }
    }

    public List<Region> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r ORDER BY r.isDefault DESC, r.name ASC",
                    Region.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Region> findByName(String name) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r WHERE r.name = :name",
                    Region.class);
            query.setParameter("name", name);
            List<Region> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Region> findCustomRegions() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r WHERE r.isDefault = false ORDER BY r.name ASC",
                    Region.class);
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

            Region region = em.find(Region.class, id);
            if (region != null) {
                em.remove(region);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete region: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete region", e);
        } finally {
            em.close();
        }
    }

    public void createDefaultRegions() {
        for (int i = 0; i < Region.DEFAULT_REGIONS.length; i++) {
            String name = Region.DEFAULT_REGIONS[i];
            String color = Region.DEFAULT_COLORS[i];

            Optional<Region> existing = findByName(name);
            if (existing.isEmpty()) {
                Region region = new Region(name, color, true);
                save(region);
                logger.debug("✓ Created default region: {}", name);
            }
        }
    }
}
