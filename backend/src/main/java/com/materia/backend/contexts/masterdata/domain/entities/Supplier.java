package com.materia.backend.contexts.masterdata.domain.entities;
import com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode;

import com.materia.backend.common.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Supplier - Version SimplifiÃ©e (MVP)
 * Architecture Hexagonale - Couche Domaine
 *
 * @author SAP MM Team
 * @version 1.0
 */
public class Supplier extends BaseEntity {

    // ============================================================
    // CONSTANTES
    // ============================================================

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    // ---- IDENTIFICATION ----
    private String code;            // Code unique
    private String name;            // Nom du fournisseur
    private String description;     // Description courte

    // ---- CONTACT ----
    private String contactPerson;   // Personne contact
    private String contactEmail;    // Email
    private String contactPhone;    // TÃ©lÃ©phone

    // ---- ADRESSE ----
    private String address;         // Adresse complÃ¨te
    private String city;            // Ville
    private String country;         // Pays
    private String postalCode;      // Code postal

    // ---- CONDITIONS COMMERCIALES ----
    private String paymentTerms;    // Conditions de paiement
    private Integer paymentDelay;   // DÃ©lai de paiement (jours)
    private CurrencyCode currencyCode;
    private String status;          // ACTIVE / INACTIVE

    // ============================================================
    // CONSTRUCTEUR DÃ‰FAUT
    // ============================================================

    public Supplier() {
        super();
        this.status = STATUS_ACTIVE;
        this.paymentDelay = 30;
        this.currencyCode = CurrencyCode.MAD;
    }

    // ============================================================
    // BUILDER SIMPLIFIÃ‰
    // ============================================================

    public static class Builder {
        private UUID id;
        private String code;
        private String name;
        private String description;
        private String contactPerson;
        private String contactEmail;
        private String contactPhone;
        private String address;
        private String city;
        private String country;
        private String postalCode;
        private String paymentTerms;
        private Integer paymentDelay = 30;
        private CurrencyCode currencyCode = com.materia.backend.contexts.masterdata.domain.enums.CurrencyCode.MAD;
        private String status = STATUS_ACTIVE;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder contactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public Builder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public Builder paymentTerms(String paymentTerms) {
            this.paymentTerms = paymentTerms;
            return this;
        }

        public Builder paymentDelay(Integer paymentDelay) {
            this.paymentDelay = paymentDelay;
            return this;
        }

        // Builder pour CurrencyCode (enum)
        public Builder currencyCode(CurrencyCode currencyCode) {
            this.currencyCode = currencyCode != null ? currencyCode : CurrencyCode.MAD;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

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

        public Supplier build() {
            if (this.id == null) this.id = UUID.randomUUID();
            if (this.createdAt == null) this.createdAt = LocalDateTime.now();
            if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();

            // Valeurs par dÃ©faut
            if (this.currencyCode == null) this.currencyCode = CurrencyCode.MAD;
            if (this.paymentDelay == null) this.paymentDelay = 30;
            if (this.status == null) this.status = STATUS_ACTIVE;

            // Validation
            if (this.code == null || this.code.trim().isEmpty()) {
                throw new IllegalArgumentException("Le code est obligatoire");
            }
            if (this.name == null || this.name.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom est obligatoire");
            }
            if (this.country == null || this.country.trim().isEmpty()) {
                throw new IllegalArgumentException("Le pays est obligatoire");
            }

            return new Supplier(this);
        }
    }

    private Supplier(Builder builder) {
        super();
        this.id = builder.id;
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.contactPerson = builder.contactPerson;
        this.contactEmail = builder.contactEmail;
        this.contactPhone = builder.contactPhone;
        this.address = builder.address;
        this.city = builder.city;
        this.country = builder.country;
        this.postalCode = builder.postalCode;
        this.paymentTerms = builder.paymentTerms;
        this.paymentDelay = builder.paymentDelay;
        this.currencyCode = builder.currencyCode;  // âœ… Enum
        this.status = builder.status;
        this.setCreatedAt(builder.createdAt);
        this.setUpdatedAt(builder.updatedAt);

        if (builder.createdBy != null) {
            this.setCreatedBy(builder.createdBy);
            this.setUpdatedBy(builder.createdBy);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // MÃ‰THODES DOMAINE
    // ============================================================

    public boolean isActive() {
        return STATUS_ACTIVE.equals(this.status);
    }

    public void activate() {
        this.status = STATUS_ACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void deactivate() {
        this.status = STATUS_INACTIVE;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (country != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        if (postalCode != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postalCode);
        }
        return sb.toString();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    // ---- IDENTIFICATION ----
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }
        this.code = code;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        this.name = name;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- CONTACT ----
    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- ADRESSE ----
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ---- CONDITIONS COMMERCIALES ----
    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public Integer getPaymentDelay() {
        return paymentDelay;
    }

    public void setPaymentDelay(Integer paymentDelay) {
        this.paymentDelay = paymentDelay != null ? paymentDelay : 30;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // âœ… Getter et Setter pour CurrencyCode (enum)
    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(CurrencyCode currencyCode) {
        if (currencyCode == null) {
            throw new IllegalArgumentException("La devise est obligatoire");
        }
        this.currencyCode = currencyCode;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // EQUALS, HASHCODE, TOSTRING
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(getId(), supplier.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
