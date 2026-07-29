package com.materia.backend.common.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseEntity {

    // ============================================================
    // 1️⃣ ATTRIBUTS DE BASE
    // ============================================================

    protected UUID id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // ============================================================
    // 2️⃣ ATTRIBUTS D'AUDIT
    // ============================================================

    /** Date de suppression (soft delete) */
    protected LocalDateTime deletedAt;

    /** Utilisateur qui a créé l'entité */
    protected String createdBy;

    /** Utilisateur qui a modifié l'entité */
    protected String updatedBy;

    /** Utilisateur qui a supprimé l'entité */
    protected String deletedBy;

    // ============================================================
    // 3️⃣ CONSTRUCTEURS
    // ============================================================

    protected BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================================
    // 4️⃣ GETTERS & SETTERS
    // ============================================================

    // ---- ID ----
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // ---- CREATED AT ----
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ---- UPDATED AT ----
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ---- DELETED AT ----
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // ---- CREATED BY ----
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // ---- UPDATED BY ----
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // ---- DELETED BY ----
    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    // ============================================================
    // 5️⃣ MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Initialise l'audit pour une nouvelle entité
     */
    public void initAudit(String userId) {
        this.createdBy = userId;
        this.updatedBy = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Met à jour l'audit pour une modification
     */
    public void updateAudit(String userId) {
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft delete - marque comme supprimé
     */
    public void softDelete(String userId) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = userId;
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si l'entité est supprimée
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Vérifie si l'entité est active (non supprimée)
     */
    public boolean isActive() {
        return this.deletedAt == null;
    }

    /**
     * Restaure une entité supprimée
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
        this.updatedAt = LocalDateTime.now();
    }

    // ============================================================
    // 6️⃣ EQUALS & HASHCODE
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // ============================================================
    // 7️⃣ TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return "BaseEntity{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
}