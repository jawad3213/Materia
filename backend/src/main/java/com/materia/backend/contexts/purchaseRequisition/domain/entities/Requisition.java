package com.materia.backend.contexts.purchaseRequisition.domain.entities;

import com.materia.backend.common.domain.BaseEntity;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionCode;
import com.materia.backend.contexts.masterdata.domain.valueObjects.Money;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Purchase Requisition Domain Entity
 * Demande d'achat avec support multi-lignes
 *
 * @author SAP MM Team
 * @version 2.0
 */
public class Requisition extends BaseEntity {

    // ============================================================
    // CONSTANTES
    // ============================================================

    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_JUSTIFICATION_LENGTH = 500;
    public static final int MAX_TITLE_LENGTH = 255;


    // ============================================================
    // ATTRIBUTS - EN-TÊTE
    // ============================================================

    // ---- IDENTIFICATION ----
    private RequisitionCode requisitionCode;  // ✅ Utilisation de RequisitionCode
    private String title;
    private String description;
    private String justification;

    // ---- STATUT ----
    private RequisitionStatus status;

    // ---- DEMANDEUR ----
    private String requesterId;
    private String requesterName;

    // ---- DATES ----
    private LocalDate requiredDate;
    private LocalDate submittedDate;
    private LocalDate approvedDate;
    private LocalDate convertedDate;

    // ---- FINANCES ----
    private Money totalAmount;
    private String currencyCode;

    // ---- APPROBATION ----
    private String approverId;
    private String approverName;
    private String rejectionReason;
    private String approvalNotes;

    // ---- COMMANDE ----
    private String purchaseOrderId;
    private String purchaseOrderCode;

    // ============================================================
    // ATTRIBUTS - LIGNES (MULTI-PRODUITS)
    // ============================================================

    private List<RequisitionLine> lines = new ArrayList<>();

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public Requisition() {
        super();
        this.lines = new ArrayList<>();
    }

    public Requisition(Builder builder) {
        super();
        this.id = builder.id;
        this.requisitionCode = builder.requisitionCode;
        this.title = builder.title;
        this.description = builder.description;
        this.justification = builder.justification;

        this.status = builder.status != null ? builder.status : RequisitionStatus.DRAFT;

        this.requesterId = builder.requesterId;
        this.requesterName = builder.requesterName;

        this.requiredDate = builder.requiredDate;
        this.submittedDate = builder.submittedDate;
        this.approvedDate = builder.approvedDate;
        this.convertedDate = builder.convertedDate;

        this.totalAmount = builder.totalAmount;
        this.currencyCode = builder.currencyCode;

        this.approverId = builder.approverId;
        this.approverName = builder.approverName;
        this.rejectionReason = builder.rejectionReason;
        this.approvalNotes = builder.approvalNotes;

        this.purchaseOrderId = builder.purchaseOrderId;
        this.purchaseOrderCode = builder.purchaseOrderCode;

        // Lignes
        this.lines = builder.lines != null ? new ArrayList<>(builder.lines) : new ArrayList<>();

        // Audit
        if (builder.createdAt != null) this.setCreatedAt(builder.createdAt);
        if (builder.updatedAt != null) this.setUpdatedAt(builder.updatedAt);
        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public RequisitionCode getRequisitionCode() {
        return requisitionCode;
    }

    public void setRequisitionCode(RequisitionCode requisitionCode) {
        this.requisitionCode = requisitionCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public RequisitionStatus getStatus() {
        return status;
    }

    public void setStatus(RequisitionStatus status) {
        this.status = status;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public LocalDate getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDate submittedDate) {
        this.submittedDate = submittedDate;
    }

    public LocalDate getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }

    public LocalDate getConvertedDate() {
        return convertedDate;
    }

    public void setConvertedDate(LocalDate convertedDate) {
        this.convertedDate = convertedDate;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getApprovalNotes() {
        return approvalNotes;
    }

    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(String purchaseOrderCode) {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public List<RequisitionLine> getLines() {
        return lines;
    }

    public void setLines(List<RequisitionLine> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    // ============================================================
    // BUILDER
    // ============================================================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private RequisitionCode requisitionCode;
        private String title;
        private String description;
        private String justification;

        private RequisitionStatus status;

        private String requesterId;
        private String requesterName;

        private LocalDate requiredDate;
        private LocalDate submittedDate;
        private LocalDate approvedDate;
        private LocalDate convertedDate;

        private Money totalAmount;
        private String currencyCode = "MAD";

        private String approverId;
        private String approverName;
        private String rejectionReason;
        private String approvalNotes;

        private String purchaseOrderId;
        private String purchaseOrderCode;

        // Lignes
        private List<RequisitionLine> lines = new ArrayList<>();

        // Audit
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // ============================================================
        // BUILDERS - EN-TÊTE
        // ============================================================

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder requisitionCode(RequisitionCode requisitionCode) {
            this.requisitionCode = requisitionCode;
            return this;
        }

        /**
         * Crée un RequisitionCode à partir d'une String
         */
        public Builder requisitionCode(String requisitionCode) {
            this.requisitionCode = RequisitionCode.of(requisitionCode);
            return this;
        }

        public Builder title(String title) {
            if (title != null && title.length() > MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException(
                        "Le titre ne peut pas dépasser " + MAX_TITLE_LENGTH + " caractères"
                );
            }
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
                throw new IllegalArgumentException(
                        "La description ne peut pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères"
                );
            }
            this.description = description;
            return this;
        }

        public Builder justification(String justification) {
            if (justification != null && justification.length() > MAX_JUSTIFICATION_LENGTH) {
                throw new IllegalArgumentException(
                        "La justification ne peut pas dépasser " + MAX_JUSTIFICATION_LENGTH + " caractères"
                );
            }
            this.justification = justification;
            return this;
        }

        public Builder status(RequisitionStatus status) {
            this.status = status;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public Builder requesterName(String requesterName) {
            this.requesterName = requesterName;
            return this;
        }

        public Builder requiredDate(LocalDate requiredDate) {
            if (requiredDate != null && requiredDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La date de besoin ne peut pas être dans le passé");
            }
            this.requiredDate = requiredDate;
            return this;
        }

        public Builder submittedDate(LocalDate submittedDate) {
            this.submittedDate = submittedDate;
            return this;
        }

        public Builder approvedDate(LocalDate approvedDate) {
            this.approvedDate = approvedDate;
            return this;
        }

        public Builder convertedDate(LocalDate convertedDate) {
            this.convertedDate = convertedDate;
            return this;
        }

        public Builder totalAmount(Money totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Builder approverId(String approverId) {
            this.approverId = approverId;
            return this;
        }

        public Builder approverName(String approverName) {
            this.approverName = approverName;
            return this;
        }

        public Builder rejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
            return this;
        }

        public Builder approvalNotes(String approvalNotes) {
            this.approvalNotes = approvalNotes;
            return this;
        }

        public Builder purchaseOrderId(String purchaseOrderId) {
            this.purchaseOrderId = purchaseOrderId;
            return this;
        }

        public Builder purchaseOrderCode(String purchaseOrderCode) {
            this.purchaseOrderCode = purchaseOrderCode;
            return this;
        }

        // ============================================================
        // BUILDERS - LIGNES
        // ============================================================

        /**
         * Ajoute une ligne à la demande
         */
        public Builder addLine(RequisitionLine line) {
            if (line == null) {
                throw new IllegalArgumentException("La ligne ne peut pas être nulle");
            }
            if (this.lines == null) {
                this.lines = new ArrayList<>();
            }
            // Assigner le numéro de ligne
            line.setLineNumber(this.lines.size() + 1);
            this.lines.add(line);
            return this;
        }

        /**
         * Ajoute plusieurs lignes
         */
        public Builder lines(List<RequisitionLine> lines) {
            if (lines == null) {
                throw new IllegalArgumentException("La liste des lignes ne peut pas être nulle");
            }
            this.lines = new ArrayList<>(lines);
            // Réassigner les numéros de ligne
            for (int i = 0; i < this.lines.size(); i++) {
                this.lines.get(i).setLineNumber(i + 1);
            }
            return this;
        }

        // ============================================================
        // BUILDERS - AUDIT
        // ============================================================

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        // ============================================================
        // VALIDATION
        // ============================================================


        // ============================================================
        // BUILD
        // ============================================================

        public Requisition build() {
            // ---- Validation ----
            validateRequiredFields();
            validateLines();

            // ---- Génération de l'ID ----
            if (this.id == null) this.id = UUID.randomUUID();

            // ---- Génération du requisitionCode ----
            if (this.requisitionCode == null) {
                this.requisitionCode = RequisitionCode.createDefault();
            }

            // ---- Dates par défaut ----
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();

            // ---- Statut par défaut ----
            if (this.status == null) this.status = RequisitionStatus.DRAFT;

            // ---- Devise par défaut ----
            if (this.currencyCode == null) this.currencyCode = "MAD";


            // ---- Calcul du total ----
            if (this.lines != null && !this.lines.isEmpty()) {
                this.totalAmount = calculateTotalFromLines(this.lines);
            }

            // ---- Calcul de la date de besoin minimale ----
            if (this.requiredDate == null && this.lines != null) {
                this.requiredDate = this.lines.stream()
                        .map(RequisitionLine::getRequiredDate)
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now().plusDays(30));
            }

            return new Requisition(this);
        }

        private void validateRequiredFields() {
            if (this.title == null || this.title.trim().isEmpty()) {
                throw new IllegalArgumentException("Le titre est obligatoire");
            }
            if (this.requesterId == null || this.requesterId.trim().isEmpty()) {
                throw new IllegalArgumentException("Le demandeur est obligatoire");
            }
            if (this.requesterName == null || this.requesterName.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom du demandeur est obligatoire");
            }
        }

        private void validateLines() {
            if (this.lines == null || this.lines.isEmpty()) {
                throw new IllegalArgumentException("Au moins une ligne est requise");
            }

            for (int i = 0; i < this.lines.size(); i++) {
                RequisitionLine line = this.lines.get(i);
                if (line.getMaterialCode() == null || line.getMaterialCode().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le matériau est obligatoire pour la ligne " + (i + 1));
                }
                if (line.getQuantity() == null || line.getQuantity() <= 0) {
                    throw new IllegalArgumentException(
                            "La quantité doit être positive pour la ligne " + (i + 1)
                    );
                }
                if (line.getMaterialName() == null || line.getMaterialName().trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "Le nom du matériau est obligatoire pour la ligne " + (i + 1)
                    );
                }
            }
        }

        private Money calculateTotalFromLines(List<RequisitionLine> lines) {
            if (lines == null || lines.isEmpty()) {
                return Money.zero(resolveCurrencyCodeForEmptyLines());
            }

            return sumLineTotals(lines);
        }

        private CurrencyCode resolveCurrencyCodeForEmptyLines() {
            if (currencyCode != null && CurrencyCode.isValidCode(currencyCode)) {
                return CurrencyCode.fromCode(currencyCode);
            }
            return CurrencyCode.MAD;
        }
    }

    // ============================================================
    // DOMAINE BEHAVIOR - GESTION DES LIGNES
    // ============================================================

    /**
     * Ajoute une ligne à la demande
     */
    public void addLine(RequisitionLine line) {
        if (line == null) {
            throw new IllegalArgumentException("La ligne ne peut pas être nulle");
        }
        // Assigner le numéro de ligne
        line.setLineNumber(this.lines.size() + 1);
        this.lines.add(line);
        recalculateTotal();
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Supprime une ligne de la demande
     */
    public void removeLine(int index) {
        if (index < 0 || index >= this.lines.size()) {
            throw new IllegalArgumentException("Index de ligne invalide: " + index);
        }
        this.lines.remove(index);
        // Réassigner les numéros de ligne
        for (int i = 0; i < this.lines.size(); i++) {
            this.lines.get(i).setLineNumber(i + 1);
        }
        recalculateTotal();
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Supprime une ligne par son ID
     */
    public void removeLine(UUID lineId) {
        if (lineId == null) {
            throw new IllegalArgumentException("L'ID de la ligne est obligatoire");
        }
        boolean removed = this.lines.removeIf(line -> lineId.equals(line.getId()));
        if (removed) {
            // Réassigner les numéros de ligne
            for (int i = 0; i < this.lines.size(); i++) {
                this.lines.get(i).setLineNumber(i + 1);
            }
            recalculateTotal();
            this.setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Met à jour une ligne
     */
    public void updateLine(int index, RequisitionLine updatedLine) {
        if (index < 0 || index >= this.lines.size()) {
            throw new IllegalArgumentException("Index de ligne invalide: " + index);
        }
        if (updatedLine == null) {
            throw new IllegalArgumentException("La ligne mise à jour ne peut pas être nulle");
        }
        // Conserver le numéro de ligne
        updatedLine.setLineNumber(index + 1);
        this.lines.set(index, updatedLine);
        recalculateTotal();
        this.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Recalcule le montant total de la demande
     */
    public void recalculateTotal() {
        if (this.lines == null || this.lines.isEmpty()) {
            CurrencyCode emptyCurrency = CurrencyCode.isValidCode(this.currencyCode)
                    ? CurrencyCode.fromCode(this.currencyCode)
                    : CurrencyCode.MAD;
            this.totalAmount = Money.zero(emptyCurrency);
            return;
        }

        Money total = sumLineTotals(this.lines);
        this.totalAmount = total;
        this.currencyCode = total.getCurrencyCode();
        this.setUpdatedAt(LocalDateTime.now());
    }

    private static Money sumLineTotals(List<RequisitionLine> lines) {
        Money total = null;

        for (RequisitionLine line : lines) {
            if (line == null || line.getLineTotal() == null) {
                continue;
            }

            if (total == null) {
                total = Money.zero(line.getLineTotal().getCurrency());
            }

            if (!total.getCurrency().equals(line.getLineTotal().getCurrency())) {
                throw new IllegalStateException("All requisition lines must use the same currency");
            }

            total = total.add(line.getLineTotal());
        }

        return total != null ? total : Money.zero(CurrencyCode.MAD);
    }

    /**
     * Calcule le nombre total de produits dans la demande
     */
    public int getTotalProductCount() {
        if (this.lines == null) {
            return 0;
        }
        return this.lines.size();
    }

    /**
     * Calcule la quantité totale demandée
     */
    public int getTotalQuantity() {
        if (this.lines == null) {
            return 0;
        }
        return this.lines.stream()
                .mapToInt(line -> line.getQuantity() != null ? line.getQuantity() : 0)
                .sum();
    }

    /**
     * Récupère les codes des matériaux de la demande
     */
    public List<String> getMaterialCodes() {
        if (this.lines == null) {
            return new ArrayList<>();
        }
        return this.lines.stream()
                .map(RequisitionLine::getMaterialCode)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    // ============================================================
    // DOMAINE BEHAVIOR - STATUT
    // ============================================================

    /**
     * Soumet la demande pour validation
     */
    public void submit(String userId) {
        if (status != RequisitionStatus.DRAFT) {
            throw new IllegalStateException("Seule une demande en brouillon peut être soumise");
        }
        if (this.lines == null || this.lines.isEmpty()) {
            throw new IllegalStateException("Impossible de soumettre une demande sans lignes");
        }
        this.status = RequisitionStatus.SUBMITTED;
        this.submittedDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    /**
     * Approuve la demande
     */
    public void approve(String approverId, String approverName, String notes) {
        if (status != RequisitionStatus.SUBMITTED && status != RequisitionStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Seule une demande soumise ou en révision peut être approuvée"
            );
        }
        this.status = RequisitionStatus.APPROVED;
        this.approverId = approverId;
        this.approverName = approverName;
        this.approvalNotes = notes;
        this.approvedDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(approverId);
    }

    /**
     * Rejette la demande
     */
    public void reject(String approverId, String approverName, String reason) {
        if (status != RequisitionStatus.SUBMITTED && status != RequisitionStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Seule une demande soumise ou en révision peut être rejetée"
            );
        }
        this.status = RequisitionStatus.REJECTED;
        this.approverId = approverId;
        this.approverName = approverName;
        this.rejectionReason = reason;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(approverId);
    }

    /**
     * Annule la demande
     */
    public void cancel(String userId, String reason) {
        if (!status.isCancellable()) {
            throw new IllegalStateException("Cette demande ne peut pas être annulée");
        }
        this.status = RequisitionStatus.CANCELLED;
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    /**
     * Convertit la demande en commande
     */
    public void convert(String purchaseOrderId, String purchaseOrderCode, String userId) {
        if (status != RequisitionStatus.APPROVED) {
            throw new IllegalStateException("Seule une demande approuvée peut être convertie");
        }
        this.status = RequisitionStatus.CONVERTED;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.convertedDate = LocalDate.now();
        this.setUpdatedAt(LocalDateTime.now());
        this.setUpdatedBy(userId);
    }

    // ============================================================
    // DOMAINE BEHAVIOR - VÉRIFICATIONS
    // ============================================================

    /**
     * Vérifie si la demande peut être modifiée
     */
    public boolean isModifiable() {
        return status != null && status.isModifiable();
    }

    /**
     * Vérifie si la demande peut être convertie
     */
    public boolean isConvertible() {
        return status != null && status.isConvertible();
    }

    /**
     * Vérifie si la demande peut être supprimée
     */
    public boolean isDeletable() {
        return status != null && status.isDeletable();
    }


    /**
     * Vérifie si la demande est en retard
     */
    public boolean isOverdue() {
        return requiredDate != null &&
                requiredDate.isBefore(LocalDate.now()) &&
                status != RequisitionStatus.CONVERTED &&
                status != RequisitionStatus.CANCELLED;
    }

    /**
     * Vérifie si la demande est entièrement reçue
     */
    public boolean isFullyReceived() {
        if (this.lines == null || this.lines.isEmpty()) {
            return false;
        }
        return this.lines.stream().allMatch(RequisitionLine::isFullyReceived);
    }

    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Requisition that = (Requisition) o;
        return Objects.equals(getId(), that.getId()) ||
                Objects.equals(requisitionCode, that.requisitionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), requisitionCode);
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return "Requisition{" +
                "id=" + getId() +
                ", requisitionCode=" + requisitionCode +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", requesterName='" + requesterName + '\'' +
                ", linesCount=" + (lines != null ? lines.size() : 0) +
                ", totalAmount=" + totalAmount +
                ", requiredDate=" + requiredDate +
                '}';
    }
}
