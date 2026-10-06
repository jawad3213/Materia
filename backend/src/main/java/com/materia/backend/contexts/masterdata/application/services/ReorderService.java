package com.materia.backend.contexts.masterData.application.services;

import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.StockStatus;
import com.materia.backend.contexts.masterData.domain.events.MaterialBelowReorderPointEvent;
import com.materia.backend.contexts.masterData.domain.events.MaterialReorderedEvent;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.masterData.domain.valueObjects.ReorderQuantity;
import com.materia.backend.contexts.purchaseRequisition.application.services.RequisitionService;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReorderService {
    
    private final MaterialRepository materialRepository;
    private final MaterialStockDomainService stockDomainService;
    private final NotificationService notificationService;
    private final RequisitionService requisitionService;
    private final ApplicationEventPublisher eventPublisher;
    
    // ============================================================
    // 1️⃣ ÉVÉNEMENT EN TEMPS RÉEL
    // ============================================================
    
    @EventListener
    public void onMaterialBelowReorderPoint(MaterialBelowReorderPointEvent event) {
        log.info("🔔 Material {} below its reorder point", event.getMaterialCode());
        
        // 1. Récupérer le matériau
        Material material = materialRepository.findById(event.getEntityId())
                .orElseThrow(() -> new RuntimeException("Material not found"));
        
        // 2. Calculer la quantité recommandée
        ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);
        
        if (reorderQuantity == null) {
            log.warn("❌ No reorder quantity calculated for {}", event.getMaterialCode());
            return;
        }

        // A requisition already in progress covers it: do not raise a second one.
        if (hasOpenRequisition(material)) {
            log.info("⏭️ A request is already open for {}, no new request", event.getMaterialCode());
            return;
        }
        
        // 3. Créer une demande d'achat automatique
        String requisitionId = requisitionService.createRequisitionFromReorder(
                material,
                reorderQuantity.getQuantity(),
                reorderQuantity.getReason(),
                reorderQuantity.isUrgent()
        );

        // Stock on order is recorded by the purchase order created from this requisition, not here:
        // a requisition is a request, and counting it would never be undone if it were rejected or cancelled.

        // 5. Publier l'événement métier
        eventPublisher.publishEvent(new MaterialReorderedEvent(
                material.getId(),
                material.getCode() != null ? material.getCode().getValue() : null,
                material.getName(),
                reorderQuantity.getQuantity(),
                requisitionId,
                material.getSupplierId(),
                reorderQuantity.isUrgent()
        ));
        
        // 4. Notifier l'acheteur
        String message = String.format(
                "📦 Automatic reorder for %s\n" +
                "Recommended quantity: %d\n" +
                "Reason: %s\n" +
                "Urgent: %s\n" +
                "Request: %s",
                material.getName(),
                reorderQuantity.getQuantity(),
                reorderQuantity.getReason(),
                reorderQuantity.isUrgent() ? "⚠️ YES" : "NO",
                requisitionId
        );
        
        notificationService.sendAlert(
                "acheteur@email.com",
                "⚠️ Automatic reorder - " + material.getName(),
                message
        );
    }

    // ============================================================
    // 2️⃣ RÉAPPROVISIONNEMENT MANUEL (1-CLIC)
    // ============================================================

    @Transactional
    public String triggerManualReorder(UUID materialId, Integer customQuantity, String customReason) {
        return triggerManualReorder(materialId, customQuantity, customReason, null);
    }

    /** One-click reorder requested by a user, who becomes the requisition's requester. */
    @Transactional
    public String triggerManualReorder(UUID materialId, Integer customQuantity, String customReason, String requesterId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException(materialId.toString()));

        ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);

        int quantity;
        String reason;
        boolean isUrgent;

        if (customQuantity != null && customQuantity > 0) {
            quantity = customQuantity;
            reason = customReason != null && !customReason.isBlank() ? customReason : "Manual reorder (one click)";
            isUrgent = material.isBelowSafetyStock();
        } else if (reorderQuantity != null) {
            quantity = reorderQuantity.getQuantity();
            reason = customReason != null && !customReason.isBlank() ? customReason : reorderQuantity.getReason();
            isUrgent = reorderQuantity.isUrgent();
        } else {
            quantity = material.getEconomicOrderQuantity() != null && material.getEconomicOrderQuantity() > 0
                    ? material.getEconomicOrderQuantity() : 100;
            reason = customReason != null && !customReason.isBlank() ? customReason : "Manual reorder (one click)";
            isUrgent = material.isBelowSafetyStock();
        }

        String requisitionId = requisitionService.createRequisitionFromReorder(
                material,
                quantity,
                reason,
                isUrgent,
                requesterId
        );

        // Publier l'événement métier
        eventPublisher.publishEvent(new MaterialReorderedEvent(
                material.getId(),
                material.getCode() != null ? material.getCode().getValue() : null,
                material.getName(),
                quantity,
                requisitionId,
                material.getSupplierId(),
                isUrgent
        ));

        log.info("✅ Manual reorder triggered for {} (Qty: {}, Request: {})",
                material.getCode() != null ? material.getCode().getValue() : material.getId(),
                quantity,
                requisitionId);

        String message = String.format(
                "📦 Manual reorder (one click) for %s\n" +
                "Ordered quantity: %d\n" +
                "Reason: %s\n" +
                "Urgent: %s\n" +
                "Purchase requisition: %s",
                material.getName(),
                quantity,
                reason,
                isUrgent ? "⚠️ YES" : "NO",
                requisitionId
        );
        notificationService.sendAlert(
                "acheteur@email.com",
                "⚠️ Manual reorder - " + material.getName(),
                message
        );

        return requisitionId;
    }

    // ============================================================
    // 3️⃣ JOB NOCTURNE (Tous les jours à 6h)
    // ============================================================
    
    @Scheduled(cron = "0 0 6 * * *")
    public void nightlyReorderCheck() {
        log.info("🌅 Starting the nightly reorder check");
        
        // 1. Récupérer tous les matériaux actifs
        List<Material> activeMaterials = materialRepository.findByStatus(MaterialStatus.ACTIVE);
        
        // 2. Filtrer ceux qui nécessitent un réapprovisionnement
        List<Material> materialsToReorder = stockDomainService.getMaterialsNeedingReorder(activeMaterials).stream()
                .filter(m -> !hasOpenRequisition(m))
                .collect(Collectors.toList());
        
        if (materialsToReorder.isEmpty()) {
            log.info("✅ No material to reorder");
            return;
        }
        
        log.info("🔔 {} materials to reorder", materialsToReorder.size());
        
        // 3. Grouper par fournisseur
        // groupingBy refuses a null key, so materials without a supplier are grouped under "" (F-020).
        var groupedBySupplier = materialsToReorder.stream()
                .collect(Collectors.groupingBy(m -> m.getSupplierId() != null ? m.getSupplierId() : ""));
        
        // 4. Pour chaque fournisseur, créer une demande groupée
        for (var entry : groupedBySupplier.entrySet()) {
            String supplierId = entry.getKey();
            List<Material> materials = entry.getValue();
            
            if (supplierId == null || supplierId.isEmpty()) {
                // Créer une demande individuelle
                for (Material material : materials) {
                    createReorderForMaterial(material);
                }
            } else {
                // Créer une demande groupée par fournisseur
                createGroupedRequisition(supplierId, materials);
            }
        }
        
        // 5. Envoyer un résumé par email
        String summary = generateReorderSummary(materialsToReorder);
        notificationService.sendReport(
                "acheteur@email.com",
                "📊 Reorder summary - " + LocalDate.now(),
                summary
        );
        
        log.info("✅ Nightly check finished");
    }
    
    private void createReorderForMaterial(Material material) {
        ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);
        if (reorderQuantity != null) {
            String requisitionId = requisitionService.createRequisitionFromReorder(
                    material,
                    reorderQuantity.getQuantity(),
                    reorderQuantity.getReason(),
                    reorderQuantity.isUrgent()
            );
            eventPublisher.publishEvent(new MaterialReorderedEvent(
                    material.getId(),
                    material.getCode() != null ? material.getCode().getValue() : null,
                    material.getName(),
                    reorderQuantity.getQuantity(),
                    requisitionId,
                    material.getSupplierId(),
                    reorderQuantity.isUrgent()
            ));
            log.info("✅ Request created for {}", material.getCode());
        }
    }
    
    private boolean hasOpenRequisition(Material material) {
        return requisitionService.hasOpenRequisitionFor(
                material.getId(), material.getCode() != null ? material.getCode().getValue() : null);
    }

    private void createGroupedRequisition(String supplierId, List<Material> materials) {
        // Créer une demande groupée pour un fournisseur
        log.info("📦 Creating a grouped request for supplier {}", supplierId);
        
        List<RequisitionLine> lines = new ArrayList<>();
        for (Material material : materials) {
            ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);
            if (reorderQuantity != null) {
                lines.add(new RequisitionLine(
                        material.getCode() != null ? material.getCode().getValue() : null,
                        reorderQuantity.getQuantity()
                ));
            }
        }
        
        requisitionService.createGroupedRequisition(supplierId, lines);
    }
    
    private String generateReorderSummary(List<Material> materials) {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 REORDER SUMMARY\n");
        sb.append("================================\n");
        sb.append("Date: ").append(LocalDate.now()).append("\n\n");
        
        for (Material material : materials) {
            StockStatus status = material.getStockStatus();
            int reorderQty = material.calculateReorderQuantity();
            sb.append(String.format(
                    "🔹 %s (%s)\n" +
                    "   - Current stock: %d\n" +
                    "   - ROP: %d\n" +
                    "   - Status: %s\n" +
                    "   - Recommended quantity: %d\n\n",
                    material.getCode() != null ? material.getCode().getValue() : "",
                    material.getName(),
                    material.getCurrentStock(),
                    material.getReorderPoint(),
                    status != null ? status.getLabel() : "",
                    reorderQty
            ));
        }
        
        return sb.toString();
    }
}
