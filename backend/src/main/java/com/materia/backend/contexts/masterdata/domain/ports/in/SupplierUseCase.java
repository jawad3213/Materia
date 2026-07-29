package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.SupplierOutput;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 */
public interface SupplierUseCase extends BaseUseCase<CreateSupplierInput, SupplierOutput, UUID> {

    List<SupplierOutput> searchSuppliers(String keyword);
}
