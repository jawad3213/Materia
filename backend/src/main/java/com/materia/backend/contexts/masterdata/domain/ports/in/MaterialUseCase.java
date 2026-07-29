package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.material.requests.CreateMaterialRequest;
import com.materia.backend.contexts.masterdata.application.dtos.material.responses.MaterialResponseDto;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Material management
 */
public interface MaterialUseCase extends BaseUseCase<CreateMaterialRequest, MaterialResponseDto, UUID> {

    List<MaterialResponseDto> getMaterialsByCategory(UUID categoryId);

    List<MaterialResponseDto> getMaterialsBySupplier(UUID supplierId);

    MaterialResponseDto increaseStock(UUID id, int quantity);

    MaterialResponseDto decreaseStock(UUID id, int quantity);

    List<MaterialResponseDto> getMaterialsBelowMinimumStock();
}
