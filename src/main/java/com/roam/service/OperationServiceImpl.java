package com.roam.service;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.repository.OperationRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of OperationService with transaction management.
 * All public methods run within transactions and handle rollback on exceptions.
 */
public class OperationServiceImpl implements OperationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationServiceImpl.class);
    private final OperationRepository repository;
    private final SearchService searchService;

    public OperationServiceImpl() {
        this.repository = new OperationRepository();
        this.searchService = SearchService.getInstance();
    }

    /**
     * Constructor for dependency injection (testing).
     */
    public OperationServiceImpl(OperationRepository repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    @Override
    public Operation createOperation(Operation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("Operation cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Operation created = repository.save(operation);

            tx.commit();
            logger.info("✓ Operation created: {}", created.getName());

            // Index after successful transaction
            indexOperation(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for operation creation");
            }
            logger.error("✗ Failed to create operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create operation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Operation updateOperation(Operation operation) {
        if (operation == null || operation.getId() == null) {
            throw new IllegalArgumentException("Operation and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Operation updated = repository.save(operation);

            tx.commit();
            logger.info("✓ Operation updated: {}", updated.getName());

            // Re-index after successful transaction
            indexOperation(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for operation update");
            }
            logger.error("✗ Failed to update operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update operation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteOperation(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Operation ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Operation> operation = repository.findById(id);
            if (operation.isPresent()) {
                repository.delete(operation.get());
                logger.info("✓ Operation deleted: {}", operation.get().getName());
            } else {
                logger.warn("⚠️ Operation not found for deletion: {}", id);
            }

            tx.commit();

            // Remove from search index
            searchService.deleteDocument(id);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for operation deletion");
            }
            logger.error("✗ Failed to delete operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete operation", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Operation> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find operation by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Operation> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all operations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operations", e);
        }
    }

    @Override
    public List<Operation> findByStatus(OperationStatus status) {
        try {
            return repository.findByStatus(status);
        } catch (Exception e) {
            logger.error("✗ Failed to find operations by status {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operations by status", e);
        }
    }

    @Override
    public List<Operation> findByPriority(Priority priority) {
        try {
            return repository.findByPriority(priority);
        } catch (Exception e) {
            logger.error("✗ Failed to find operations by priority {}: {}", priority, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operations by priority", e);
        }
    }

    @Override
    public List<Operation> findDueBefore(LocalDate date) {
        try {
            // Operation doesn't have dueDate field
            // This method may not be needed or should query related tasks
            return List.of();
        } catch (Exception e) {
            logger.error("✗ Failed to find operations due before date: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve operations", e);
        }
    }

    @Override
    public List<Operation> findRecentlyUpdated() {
        try {
            List<Operation> allOperations = repository.findAll();
            return allOperations.stream()
                    .sorted((o1, o2) -> o2.getUpdatedAt().compareTo(o1.getUpdatedAt()))
                    .limit(5)
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find recently updated operations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recently updated operations", e);
        }
    }

    @Override
    public long count() {
        try {
            return repository.count();
        } catch (Exception e) {
            logger.error("✗ Failed to count operations: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long countByStatus(OperationStatus status) {
        try {
            return repository.findByStatus(status).size();
        } catch (Exception e) {
            logger.error("✗ Failed to count operations by status: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public void indexOperation(Operation operation) {
        try {
            searchService.indexOperation(
                    operation.getId(),
                    operation.getName(),
                    operation.getPurpose(),
                    operation.getOutcome(),
                    operation.getStatus() != null ? operation.getStatus().toString() : null,
                    operation.getPriority() != null ? operation.getPriority().toString() : null);
            logger.debug("✓ Operation indexed: {}", operation.getName());
        } catch (Exception e) {
            logger.error("✗ Failed to index operation: {}", e.getMessage(), e);
            // Don't throw - indexing failure shouldn't fail the operation
        }
    }
}
