package com.materia.backend.common.domain;

import java.util.List;
import java.util.Optional;

/**
 * Base Use Case interface for common domain operations.
 * Hexagonal Architecture - Defines common use cases available to the outside.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's identifier
 */
public interface BaseUseCase<T, ID> {

    T create(T entity);

    T update(ID id, T entityDetails);

    void delete(ID id);

    Optional<T> getById(ID id);

    Optional<T> getByCode(String code);

    List<T> getAll();
}
