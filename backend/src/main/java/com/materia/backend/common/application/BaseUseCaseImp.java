package com.materia.backend.common.application;

/**
 * Base Use Case interface
 * Represents a single unit of business functionality
 * Part of the application layer - orchestrates domain objects
 *
 * @param <I> Input request type
 * @param <O> Output response type
 */
public interface BaseUseCaseImp<I extends BaseRequest, O extends BaseResponse> {

    /**
     * Execute the use case
     * @param request Input data
     * @return Output data
     */
    O execute(I request);

    /**
     * Get the name of the use case (for logging/monitoring)
     */
    default String getUseCaseName() {
        return this.getClass().getSimpleName();
    }
}


