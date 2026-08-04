package com.materia.backend.contexts.purchaseOrder.domain.ports.in;

import com.materia.backend.common.domain.BaseUseCase;
import com.materia.backend.contexts.purchaseOrder.application.dtos.CreatePurchaseOrderInput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.PurchaseOrderOutput;
import com.materia.backend.contexts.purchaseOrder.application.dtos.UpdatePurchaseOrderInput;

import java.util.List;
import java.util.UUID;

/**
 * Input port for purchase order use cases.
 */
public interface PurchaseOrderUseCase extends BaseUseCase<CreatePurchaseOrderInput, PurchaseOrderOutput, UUID> {

    PurchaseOrderOutput update(UUID id, UpdatePurchaseOrderInput request);

    List<PurchaseOrderOutput> getByStatus(String status);

    List<PurchaseOrderOutput> getByDeliveryStatus(String deliveryStatus);

    List<PurchaseOrderOutput> getBySupplierId(UUID supplierId);

    List<PurchaseOrderOutput> getByRequisitionId(UUID requisitionId);

    List<PurchaseOrderOutput> searchByKeyword(String keyword);

    PurchaseOrderOutput submit(UUID id, String userId);

    PurchaseOrderOutput confirm(UUID id, String userId);

    PurchaseOrderOutput assignReceiver(UUID id, String userId, String userName, String assignedUserId, String assignedUserName);

    PurchaseOrderOutput confirmReceipt(UUID id, String receiverId, String receiverName);

    PurchaseOrderOutput cancel(UUID id, String userId, String reason);

    PurchaseOrderOutput complete(UUID id, String userId);

    PurchaseOrderOutput updateDeliveryStatus(UUID id, String deliveryStatus, String userId);
}
