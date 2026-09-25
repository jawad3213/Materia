package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier;
import com.materia.backend.common.domain.enums.CurrencyCode;

/**
 * Request payload for advanced supplier search.
 * Supports searching by individual fields (code, name, description, contactPerson, contactEmail, fullAddress)
 * and filtering by status, currencyCode, country.
 */
public class SupplierSearchWebRequest {

    // ---- Search fields ----
    private String code;
    private String name;
    private String description;
    private String contactPerson;
    private String contactEmail;
    private String fullAddress;

    // ---- Filter fields ----
    private String status;
    private String currencyCode;
    private String country;

    public SupplierSearchWebRequest() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
