package com.materia.backend.common.application;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base Mapper interface for converting between entities and DTOs
 * Use MapStruct or custom implementations in infrastructure layer
 *
 * @param <E> Entity type (Domain)
 * @param <CR> Create Request DTO type
 * @param <UR> Update Request DTO type
 * @param <D> Response DTO type
 */
public interface BaseMapper<E, CR extends BaseInput, UR extends BaseInput, D extends BaseOutput> {

    /**
     * Convert Entity to Response DTO
     */
    D toResponse(E entity);

    /**
     * Convert Create Request DTO to Entity
     */
    E toEntity(CR request);

    /**
     * Convert Update Request DTO to existing Entity (partial update)
     */
    void updateEntity(E entity, UR request);

    /**
     * Convert list of Entities to list of Response DTOs
     */
    default List<D> toResponseList(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Create Request DTOs to list of Entities
     */
    default List<E> toEntities(List<CR> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}