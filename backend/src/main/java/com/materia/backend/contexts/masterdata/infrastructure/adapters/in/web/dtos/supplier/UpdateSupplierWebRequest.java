package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class UpdateSupplierWebRequest {
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters if provided")
    private String name;
    
    @Size(min = 1, max = 100, message = "Contact person must be between 1 and 100 characters if provided")
    private String contactPerson;
    
    @Email(message = "Contact email must be valid if provided")
    private String contactEmail;
    
    @Size(min = 1, max = 20, message = "Contact phone must be between 1 and 20 characters if provided")
    private String contactPhone;
    
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String paymentTerms;
    
    @PositiveOrZero(message = "Payment delay cannot be negative")
    private Integer paymentDelay;
    
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters if provided")
    private String currencyCode;
    private String status;
    private String updatedBy;

    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
