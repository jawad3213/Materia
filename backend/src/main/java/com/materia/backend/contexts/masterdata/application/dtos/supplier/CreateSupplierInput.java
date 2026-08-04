package com.materia.backend.contexts.masterData.application.dtos.supplier;

import com.materia.backend.common.application.BaseInput;

/**
 * Request DTO for creating a new Supplier
 */
public class CreateSupplierInput extends BaseInput {

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
    private Integer paymentDelay;
    private String currencyCode;
    private String createdBy;

    // Getters and Setters
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

    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public Integer getPaymentDelay() { return paymentDelay; }
    public void setPaymentDelay(Integer paymentDelay) { this.paymentDelay = paymentDelay; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
