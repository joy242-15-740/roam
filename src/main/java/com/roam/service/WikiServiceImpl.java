package com.roam.service;

import com.roam.model.Wiki;
import com.roam.repository.WikiRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of WikiService with transaction management.
 */
public class WikiServiceImpl implements WikiService {

    private static final Logger logger = LoggerFactory.getLogger(WikiServiceImpl.class);
    private final WikiRepository repository;
    private final SearchService searchService;

    public WikiServiceImpl() {
        this.repository = new WikiRepository();
        this.searchService = SearchService.getInstance();
    }

    public WikiServiceImpl(WikiRepository repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    @Override
    public Wiki createWiki(Wiki wiki) {
        if (wiki == null) {
            throw new IllegalArgumentException("Wiki cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Wiki created = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki created: {}", created.getTitle());

            indexWiki(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki creation");
            }
            logger.error("✗ Failed to create wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create wiki", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Wiki updateWiki(Wiki wiki) {
        if (wiki == null || wiki.getId() == null) {
            throw new IllegalArgumentException("Wiki and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Wiki updated = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki updated: {}", updated.getTitle());

            indexWiki(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki update");
            }
            logger.error("✗ Failed to update wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update wiki", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteWiki(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Wiki ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Wiki> wiki = repository.findById(id);
            if (wiki.isPresent()) {
                repository.delete(wiki.get());
                logger.info("✓ Wiki deleted: {}", wiki.get().getTitle());
            } else {
                logger.warn("⚠️ Wiki not found for deletion: {}", id);
            }

            tx.commit();

            searchService.deleteDocument(id);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki deletion");
            }
            logger.error("✗ Failed to delete wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete wiki", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Wiki> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find wiki by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Wiki> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis", e);
        }
    }

    @Override
    public List<Wiki> findByOperationId(Long operationId) {
        try {
            return repository.findByOperationId(operationId);
        } catch (Exception e) {
            logger.error("✗ Failed to find wikis by operation ID {}: {}", operationId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis by operation", e);
        }
    }

    @Override
    public List<Wiki> findByTaskId(Long taskId) {
        try {
            // WikiRepository doesn't have findByTaskId method
            // Need to query by taskId field
            return repository.findAll().stream()
                    .filter(w -> taskId.equals(w.getTaskId()))
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find wikis by task ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis", e);
        }
    }

    @Override
    public List<Wiki> findFavorites() {
        try {
            return repository.findFavorites();
        } catch (Exception e) {
            logger.error("✗ Failed to find favorite wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve favorite wikis", e);
        }
    }

    @Override
    public List<Wiki> findRecent(int limit) {
        try {
            return repository.findRecent(limit);
        } catch (Exception e) {
            logger.error("✗ Failed to find recent wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent wikis", e);
        }
    }

    @Override
    public Wiki toggleFavorite(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Wiki ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Wiki> wikiOpt = repository.findById(id);
            if (wikiOpt.isEmpty()) {
                throw new IllegalArgumentException("Wiki not found: " + id);
            }

            Wiki wiki = wikiOpt.get();
            wiki.setIsFavorite(!wiki.getIsFavorite());
            Wiki updated = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki favorite toggled: {} = {}", updated.getTitle(), updated.getIsFavorite());

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki favorite toggle");
            }
            logger.error("✗ Failed to toggle wiki favorite: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to toggle wiki favorite", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void indexWiki(Wiki wiki) {
        try {
            searchService.indexWiki(
                    wiki.getId(),
                    wiki.getTitle(),
                    wiki.getContent(),
                    wiki.getRegion(),
                    wiki.getOperationId(),
                    wiki.getUpdatedAt());
            logger.debug("✓ Wiki indexed: {}", wiki.getTitle());
        } catch (Exception e) {
            logger.error("✗ Failed to index wiki: {}", e.getMessage(), e);
        }
    }
}
