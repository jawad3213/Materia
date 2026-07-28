package com.materia.backend.common.application;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base Mapper interface for converting between entities and DTOs
 * Use MapStruct or custom implementations in infrastructure layer
 *
 * @param <E> Entity type (Domain)
 * @param <R> Request DTO type
 * @param <D> Response DTO type
 */
public interface BaseMapper<E, R extends BaseRequest, D extends BaseResponse> {

    /**
     * Convert Entity to Response DTO
     */
    D toResponse(E entity);

    /**
     * Convert Request DTO to Entity
     */
    E toEntity(R request);

    /**
     * Convert Request DTO to existing Entity (partial update)
     */
    void updateEntity(R request, E entity);

    /**
     * Convert list of Entities to list of Response DTOs
     */
    default List<D> toResponses(List<E> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Request DTOs to list of Entities
     */
    default List<E> toEntities(List<R> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}