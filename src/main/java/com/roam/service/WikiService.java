package com.roam.service;

import com.roam.model.Wiki;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Wiki (note) business logic and transaction management.
 */
public interface WikiService {

    /**
     * Create a new wiki note.
     * 
     * @param wiki Wiki to create
     * @return Created wiki with generated ID
     */
    Wiki createWiki(Wiki wiki);

    /**
     * Update an existing wiki note.
     * 
     * @param wiki Wiki to update
     * @return Updated wiki
     */
    Wiki updateWiki(Wiki wiki);

    /**
     * Delete a wiki note by ID.
     * 
     * @param id Wiki ID
     */
    void deleteWiki(Long id);

    /**
     * Find wiki by ID.
     * 
     * @param id Wiki ID
     * @return Optional containing wiki if found
     */
    Optional<Wiki> findById(Long id);

    /**
     * Find all wikis ordered by updated date (newest first).
     * 
     * @return List of all wikis
     */
    List<Wiki> findAll();

    /**
     * Find wikis by operation ID.
     * 
     * @param operationId Operation ID
     * @return List of wikis for operation
     */
    List<Wiki> findByOperationId(Long operationId);

    /**
     * Find wikis by task ID.
     * 
     * @param taskId Task ID
     * @return List of wikis for task
     */
    List<Wiki> findByTaskId(Long taskId);

    /**
     * Find favorite wikis.
     * 
     * @return List of favorite wikis
     */
    List<Wiki> findFavorites();

    /**
     * Find recent wikis (limited number).
     * 
     * @param limit Maximum number of wikis to return
     * @return List of recent wikis
     */
    List<Wiki> findRecent(int limit);

    /**
     * Toggle favorite status of a wiki.
     * 
     * @param id Wiki ID
     * @return Updated wiki
     */
    Wiki toggleFavorite(Long id);

    /**
     * Index wiki in search service.
     * 
     * @param wiki Wiki to index
     */
    void indexWiki(Wiki wiki);
}
