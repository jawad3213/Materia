package com.materia.backend.contexts.masterData.application.dtos.supplier;

import com.materia.backend.common.application.BaseOutput;
import java.util.UUID;

/**
 * Lightweight Response DTO for Supplier lists
 */
public class SupplierListOutput extends BaseOutput {

    private UUID id;
    private String code;
    private String name;
    private String contactPerson;
    private String contactEmail;
    private String contactPhone;
    private String city;
    private String country;
    private String currencyCode;
    private String status;

    public SupplierListOutput() { super(); }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
