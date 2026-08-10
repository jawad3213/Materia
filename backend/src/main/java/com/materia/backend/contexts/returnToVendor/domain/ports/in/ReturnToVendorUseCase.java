package com.materia.backend.contexts.returnToVendor.domain.ports.in;

import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.returnToVendor.application.dtos.CreateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorLineInput;
import com.materia.backend.contexts.returnToVendor.application.dtos.ReturnToVendorOutput;
import com.materia.backend.contexts.returnToVendor.application.dtos.UpdateReturnToVendorInput;
import com.materia.backend.contexts.returnToVendor.domain.enums.ResolutionType;
import com.materia.backend.contexts.returnToVendor.domain.enums.ReturnStatus;

import java.util.List;
import java.util.UUID;

public interface ReturnToVendorUseCase
        extends BaseUseCase<CreateReturnToVendorInput, ReturnToVendorOutput, UUID> {

    ReturnToVendorOutput update(UUID id, UpdateReturnToVendorInput request);

    void delete(UUID id, String userId);

    List<ReturnToVendorOutput> getByGoodsReceiptId(String goodsReceiptId);

    List<ReturnToVendorOutput> getByPurchaseOrderId(String purchaseOrderId);

    List<ReturnToVendorOutput> getBySupplierId(String supplierId);

    List<ReturnToVendorOutput> getByStatus(ReturnStatus status);

    List<ReturnToVendorOutput> search(String keyword);

    ReturnToVendorOutput addLine(UUID id, ReturnToVendorLineInput line, String userId);

    ReturnToVendorOutput removeLine(UUID id, int lineIndex, String userId);

    ReturnToVendorOutput submit(UUID id, String userId);

    ReturnToVendorOutput resolve(UUID id, String userId, ResolutionType resolutionType, String reference);

    ReturnToVendorOutput cancel(UUID id, String userId, String reason);
}
