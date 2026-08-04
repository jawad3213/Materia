package com.materia.backend.common.application;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Shared base class for application services built on top of the common repository and mapper contracts.
 */
public abstract class AbstractCrudApplicationService<
        E extends BaseEntity,
        CR extends BaseInput,
        UR extends BaseInput,
        O extends BaseOutput> {

    private final BaseRepository<E> repository;
    private final BaseMapper<E, CR, UR, O> mapper;

    protected AbstractCrudApplicationService(BaseRepository<E> repository,
                                             BaseMapper<E, CR, UR, O> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    protected BaseRepository<E> repository() {
        return repository;
    }

    protected BaseMapper<E, CR, UR, O> mapper() {
        return mapper;
    }

    protected E saveEntity(E entity) {
        return repository.save(entity);
    }

    protected List<O> toResponseList(List<E> entities) {
        return mapper.toResponseList(entities);
    }

    protected O toResponse(E entity) {
        return mapper.toResponse(entity);
    }

    protected List<O> getAllResponses() {
        return mapper.toResponseList(repository.findAll());
    }

    protected E getEntityByIdOrThrow(UUID id, Supplier<? extends RuntimeException> notFoundExceptionSupplier) {
        return repository.findById(id)
                .orElseThrow(notFoundExceptionSupplier);
    }
}
