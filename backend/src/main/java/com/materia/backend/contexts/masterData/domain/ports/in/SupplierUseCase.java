package com.materia.backend.contexts.masterData.domain.ports.in;

import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria;
import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.domain.BaseUseCase;

import java.util.List;
import java.util.UUID;

/**
 * Input Port (Use Case) for Supplier management
 */
public interface SupplierUseCase extends BaseUseCase<CreateSupplierInput, SupplierOutput, UUID> {

    SupplierOutput update(UUID id, UpdateSupplierInput request);

    List<SupplierOutput> searchSuppliers(String keyword);

    PageResponse<SupplierListOutput> getAllList(int page, int size);

    PageResponse<SupplierListOutput> filterList(SupplierFilterCriteria criteria, int page, int size);

    PageResponse<SupplierOutput> searchAdvancedList(SupplierSearchCriteria criteria, int page, int size);
}
