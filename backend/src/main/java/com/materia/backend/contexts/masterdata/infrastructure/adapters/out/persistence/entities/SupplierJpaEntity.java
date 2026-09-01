package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence.entities;

import com.materia.backend.common.infrastructure.persistence.BaseJpaEntity;
import com.materia.backend.contexts.masterData.domain.enums.CurrencyCode;

import java.util.List;

import jakarta.persistence.*;

/**
 * JPA Entity for Supplier
 * Infrastructure Layer - Maps to the 'suppliers' database table
 */
@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_code", columnList = "code", unique = true),
        @Index(name = "idx_supplier_status", columnList = "status"),
        @Index(name = "idx_supplier_country", columnList = "country"),
        @Index(name = "idx_supplier_city", columnList = "city")
})
public class SupplierJpaEntity extends BaseJpaEntity {

    // ---- IDENTIFICATION ----
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    // ---- CONTACT ----
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // ---- ADDRESS ----
    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // ---- COMMERCIAL TERMS ----
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "supplier_payment_terms", joinColumns = @JoinColumn(name = "supplier_id"))
    @Column(name = "payment_term")
    private List<String> paymentTerms;

    @Column(name = "payment_delay")
    private Integer paymentDelay;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_code", length = 10)
    private CurrencyCode currencyCode;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public SupplierJpaEntity() {
        super();
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public List<String> getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(List<String> paymentTerms) { this.paymentTerms = paymentTerms; }

    public Integer getPaymentDelay() { return paymentDelay; }
    public void setPaymentDelay(Integer paymentDelay) { this.paymentDelay = paymentDelay; }

    public CurrencyCode getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(CurrencyCode currencyCode) { this.currencyCode = currencyCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
