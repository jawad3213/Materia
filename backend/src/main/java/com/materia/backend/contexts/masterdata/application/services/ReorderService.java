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
        log.info("🔔 Matériau {} en dessous du point de réapprovisionnement", event.getMaterialCode());
        
        // 1. Récupérer le matériau
        Material material = materialRepository.findById(event.getEntityId())
                .orElseThrow(() -> new RuntimeException("Matériau non trouvé"));
        
        // 2. Calculer la quantité recommandée
        ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);
        
        if (reorderQuantity == null) {
            log.warn("❌ Pas de quantité de réapprovisionnement calculée pour {}", event.getMaterialCode());
            return;
        }
        
        // 3. Créer une demande d'achat automatique
        String requisitionId = requisitionService.createRequisitionFromReorder(
                material,
                reorderQuantity.getQuantity(),
                reorderQuantity.getReason(),
                reorderQuantity.isUrgent()
        );

        // 4. Mettre à jour le stock en commande
        material.addStockOnOrder(reorderQuantity.getQuantity());
        materialRepository.save(material);

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
                "📦 Réapprovisionnement automatique pour %s\n" +
                "Quantité recommandée: %d\n" +
                "Raison: %s\n" +
                "Urgent: %s\n" +
                "Demande: %s",
                material.getName(),
                reorderQuantity.getQuantity(),
                reorderQuantity.getReason(),
                reorderQuantity.isUrgent() ? "⚠️ OUI" : "NON",
                requisitionId
        );
        
        notificationService.sendAlert(
                "acheteur@email.com",
                "⚠️ Réapprovisionnement automatique - " + material.getName(),
                message
        );
    }

    // ============================================================
    // 2️⃣ RÉAPPROVISIONNEMENT MANUEL (1-CLIC)
    // ============================================================

    @Transactional
    public String triggerManualReorder(UUID materialId, Integer customQuantity, String customReason) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new com.materia.backend.contexts.masterData.domain.exceptions.MaterialNotFoundException(materialId.toString()));

        ReorderQuantity reorderQuantity = stockDomainService.getRecommendedReorderQuantity(material);

        int quantity;
        String reason;
        boolean isUrgent;

        if (customQuantity != null && customQuantity > 0) {
            quantity = customQuantity;
            reason = customReason != null && !customReason.isBlank() ? customReason : "Réapprovisionnement manuel (1-clic)";
            isUrgent = material.isBelowSafetyStock();
        } else if (reorderQuantity != null) {
            quantity = reorderQuantity.getQuantity();
            reason = customReason != null && !customReason.isBlank() ? customReason : reorderQuantity.getReason();
            isUrgent = reorderQuantity.isUrgent();
        } else {
            quantity = material.getEconomicOrderQuantity() != null && material.getEconomicOrderQuantity() > 0
                    ? material.getEconomicOrderQuantity() : 100;
            reason = customReason != null && !customReason.isBlank() ? customReason : "Réapprovisionnement manuel (1-clic)";
            isUrgent = material.isBelowSafetyStock();
        }

        String requisitionId = requisitionService.createRequisitionFromReorder(
                material,
                quantity,
                reason,
                isUrgent
        );

        // Mettre à jour le stock en commande sur le matériau
        material.addStockOnOrder(quantity);
        materialRepository.save(material);

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

        log.info("✅ Réapprovisionnement manuel déclenché pour {} (Qté: {}, Demande: {})",
                material.getCode() != null ? material.getCode().getValue() : material.getId(),
                quantity,
                requisitionId);

        String message = String.format(
                "📦 Réapprovisionnement manuel (1-clic) pour %s\n" +
                "Quantité commandée: %d\n" +
                "Raison: %s\n" +
                "Urgent: %s\n" +
                "Demande d'achat: %s",
                material.getName(),
                quantity,
                reason,
                isUrgent ? "⚠️ OUI" : "NON",
                requisitionId
        );
        notificationService.sendAlert(
                "acheteur@email.com",
                "⚠️ Réapprovisionnement manuel - " + material.getName(),
                message
        );

        return requisitionId;
    }

    // ============================================================
    // 3️⃣ JOB NOCTURNE (Tous les jours à 6h)
    // ============================================================
    
    @Scheduled(cron = "0 0 6 * * *")
    public void nightlyReorderCheck() {
        log.info("🌅 Début de la vérification nocturne des réapprovisionnements");
        
        // 1. Récupérer tous les matériaux actifs
        List<Material> activeMaterials = materialRepository.findByStatus(MaterialStatus.ACTIVE);
        
        // 2. Filtrer ceux qui nécessitent un réapprovisionnement
        List<Material> materialsToReorder = stockDomainService.getMaterialsNeedingReorder(activeMaterials);
        
        if (materialsToReorder.isEmpty()) {
            log.info("✅ Aucun matériau à réapprovisionner");
            return;
        }
        
        log.info("🔔 {} matériaux à réapprovisionner", materialsToReorder.size());
        
        // 3. Grouper par fournisseur
        var groupedBySupplier = materialsToReorder.stream()
                .collect(Collectors.groupingBy(Material::getSupplierId));
        
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
                "📊 Résumé des réapprovisionnements - " + LocalDate.now(),
                summary
        );
        
        log.info("✅ Vérification nocturne terminée");
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
            material.addStockOnOrder(reorderQuantity.getQuantity());
            materialRepository.save(material);

            eventPublisher.publishEvent(new MaterialReorderedEvent(
                    material.getId(),
                    material.getCode() != null ? material.getCode().getValue() : null,
                    material.getName(),
                    reorderQuantity.getQuantity(),
                    requisitionId,
                    material.getSupplierId(),
                    reorderQuantity.isUrgent()
            ));
            log.info("✅ Demande créée pour {}", material.getCode());
        }
    }
    
    private void createGroupedRequisition(String supplierId, List<Material> materials) {
        // Créer une demande groupée pour un fournisseur
        log.info("📦 Création d'une demande groupée pour le fournisseur {}", supplierId);
        
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
        sb.append("📊 RÉSUMÉ DES RÉAPPROVISIONNEMENTS\n");
        sb.append("================================\n");
        sb.append("Date: ").append(LocalDate.now()).append("\n\n");
        
        for (Material material : materials) {
            StockStatus status = material.getStockStatus();
            int reorderQty = material.calculateReorderQuantity();
            sb.append(String.format(
                    "🔹 %s (%s)\n" +
                    "   - Stock actuel: %d\n" +
                    "   - ROP: %d\n" +
                    "   - Status: %s\n" +
                    "   - Quantité recommandée: %d\n\n",
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
