package com.materia.backend.contexts.masterData.domain.ports.in;

import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 */
public interface SupplierUseCase extends BaseUseCase<CreateSupplierInput, SupplierOutput, UUID> {

    SupplierOutput update(UUID id, UpdateSupplierInput request);

    List<SupplierOutput> searchSuppliers(String keyword);
}
