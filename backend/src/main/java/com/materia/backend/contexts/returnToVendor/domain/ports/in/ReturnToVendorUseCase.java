package com.materia.backend.contexts.returnToVendor.domain.ports.in;

import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;

import java.util.List;
import java.util.UUID;

/** Returns to vendor: goods rejected at receipt, sent back, then replaced or credited by the supplier. */
public interface ReturnToVendorUseCase {

    ReturnToVendorOutput create(CreateReturnToVendorInput request);

    ReturnToVendorOutput update(UUID id, UpdateReturnToVendorInput request);

    void delete(UUID id, String userId);

    ReturnToVendorOutput getById(UUID id);

    ReturnToVendorOutput getByCode(String code);

    List<ReturnToVendorOutput> getAll();

    List<ReturnToVendorOutput> getByGoodsReceiptId(String goodsReceiptId);

    List<ReturnToVendorOutput> getByPurchaseOrderId(String purchaseOrderId);

    List<ReturnToVendorOutput> getBySupplierId(String supplierId);

    List<ReturnToVendorOutput> getByStatus(ReturnStatus status);

    List<ReturnToVendorOutput> search(String keyword);

    ReturnToVendorOutput submit(UUID id, String userId);

    ReturnToVendorOutput resolve(UUID id, String userId, ResolutionType resolutionType, String reference, String supplierResponse);

    ReturnToVendorOutput cancel(UUID id, String userId, String reason);
}
