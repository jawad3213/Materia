package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier;
import com.materia.backend.common.domain.enums.CurrencyCode;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateSupplierWebRequest {
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Name is mandatory")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Contact person is mandatory")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;
    
    @NotBlank(message = "Contact email is mandatory")
    @Email(message = "Contact email must be valid")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is mandatory")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;
    
    @NotBlank(message = "Address is mandatory")
    private String address;
    
    @NotBlank(message = "City is mandatory")
    private String city;
    

    @NotBlank(message = "Country is mandatory")
    private String country;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    @NotEmpty(message = "Payment terms are mandatory")
    private List<String> paymentTerms;

    @PositiveOrZero(message = "Payment delay cannot be negative")
    private Integer paymentDelay;

    @NotBlank(message = "Currency code is mandatory")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode;

    @NotBlank(message = "Created by is mandatory")
    private String createdBy;

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
    public List<String> getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(List<String> paymentTerms) { this.paymentTerms = paymentTerms; }
    public Integer getPaymentDelay() { return paymentDelay; }
    public void setPaymentDelay(Integer paymentDelay) { this.paymentDelay = paymentDelay; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
