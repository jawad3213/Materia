package com.materia.backend.contexts.masterData.domain.events;

import com.materia.backend.common.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement : Matériau réapprovisionné
 * Déclenché lorsqu'un réapprovisionnement (manuel 1-clic ou automatique) est déclenché pour un matériau.
 * 
 * @author SAP MM Team
 * @version 1.1
 */
public class MaterialReorderedEvent extends DomainEvent {
    
    private final String materialCode;
    private final String materialName;
    private final int reorderQuantity;
    private final String requisitionId;
    private final String purchaseOrderId;
    private final String supplierId;
    private final boolean isUrgent;
    
    /**
     * Constructeur standard : au moment du réapprovisionnement, la demande d'achat (requisitionId)
     * est créée mais la commande d'achat (purchaseOrderId) n'existe pas encore.
     */
    public MaterialReorderedEvent(UUID materialId,
                                  String materialCode,
                                  String materialName,
                                  int reorderQuantity,
                                  String requisitionId,
                                  String supplierId,
                                  boolean isUrgent) {
        this(materialId, materialCode, materialName, reorderQuantity, requisitionId, null, supplierId, isUrgent);
    }
    
    /**
     * Constructeur complet (si le bon de commande purchaseOrderId est déjà connu)
     */
    public MaterialReorderedEvent(UUID materialId,
                                  String materialCode,
                                  String materialName,
                                  int reorderQuantity,
                                  String requisitionId,
                                  String purchaseOrderId,
                                  String supplierId,
                                  boolean isUrgent) {
        super(materialId);
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.reorderQuantity = reorderQuantity;
        this.requisitionId = requisitionId;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.isUrgent = isUrgent;
    }

    /**
     * Constructeur de reconstitution (pour l'Event Store / Audit / Replay)
     */
    public MaterialReorderedEvent(UUID eventId,
                                  LocalDateTime occurredOn,
                                  UUID materialId,
                                  String materialCode,
                                  String materialName,
                                  int reorderQuantity,
                                  String requisitionId,
                                  String purchaseOrderId,
                                  String supplierId,
                                  boolean isUrgent) {
        super(eventId, occurredOn, materialId);
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.reorderQuantity = reorderQuantity;
        this.requisitionId = requisitionId;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.isUrgent = isUrgent;
    }
    
    public UUID getMaterialId() {
        return (UUID) getAggregateId();
    }
    
    public UUID getEntityId() {
        return getMaterialId();
    }
    
    public LocalDateTime getOccurredAt() {
        return getOccurredOn();
    }
    
    @Override
    public String getEventType() {
        return "MATERIAL_REORDERED";
    }
    
    public int getVersion() {
        return 1;
    }
    
    public String getMaterialCode() { return materialCode; }
    public String getMaterialName() { return materialName; }
    public int getReorderQuantity() { return reorderQuantity; }
    public String getRequisitionId() { return requisitionId; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public String getSupplierId() { return supplierId; }
    public boolean isUrgent() { return isUrgent; }
}
