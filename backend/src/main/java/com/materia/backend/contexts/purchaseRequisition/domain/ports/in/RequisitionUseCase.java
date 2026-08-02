package com.materia.backend.contexts.purchaseRequisition.domain.ports.in;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionSearchCriteria;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;

import java.util.List;
import java.util.UUID;

/**
 * Input port for purchase requisition use cases.
 */
public interface RequisitionUseCase extends BaseUseCase<CreateRequisitionInput, RequisitionOutput, UUID> {

    RequisitionOutput update(UUID id, UpdateRequisitionInput requisition);

    List<RequisitionOutput> getByStatus(String status);

    List<RequisitionOutput> getByRequesterId(String requesterId);

    List<RequisitionOutput> searchByKeyword(String keyword);

    PageResponse<RequisitionOutput> searchAdvanced(RequisitionSearchCriteria criteria, int page, int size);

    RequisitionOutput submit(UUID id, String userId);

    RequisitionOutput approve(UUID id, String approverId, String approverName, String notes);

    RequisitionOutput reject(UUID id, String approverId, String approverName, String reason);

    RequisitionOutput cancel(UUID id, String userId, String reason);

    RequisitionOutput convert(UUID id, String purchaseOrderId, String purchaseOrderCode, String userId);
}
