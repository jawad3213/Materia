package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.CreateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.responses.SupplierResponseDto;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 */
public interface SupplierUseCase extends BaseUseCase<CreateSupplierRequest, SupplierResponseDto, UUID> {

    List<SupplierResponseDto> searchSuppliers(String keyword);
}
