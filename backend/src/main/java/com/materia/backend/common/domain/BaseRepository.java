package com.materia.backend.common.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base Repository interface (Port) - PURE INTERFACE
 * Defines the contract for data access operations
 * This is a port in Hexagonal Architecture
 * NO framework dependencies
 */
public interface BaseRepository<T extends BaseEntity> {

    /**
     * Find entity by ID
     */
    Optional<T> findById(UUID uuid);

    /**
     * Find all entities (excluding deleted)
     */
    List<T> findAll();


    /**
     * Save an entity
     */
    T save(T entity);

    /**
     * Save all entities
     */
    List<T> saveAll(List<T> entities);

    /**
     * Delete by ID (soft delete)
     */
    void deleteById(UUID uuid);

    /**
     * Delete an entity (soft delete)
     */
    void delete(T entity);


    /**
     * Check if entity exists
     */
    boolean existsById(UUID id);

    /**
     * Count all entities
     */
    long count();

    /**
     * Find by IDs
     */
    List<T> findAllById(List<UUID> ids);
}