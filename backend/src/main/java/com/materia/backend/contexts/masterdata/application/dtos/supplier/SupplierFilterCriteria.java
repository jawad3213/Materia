package com.materia.backend.contexts.masterData.application.dtos.supplier;

/**
 * Dedicated criteria for UI filtering (Status, Currency, Country).
 */
public class SupplierFilterCriteria {

    private String status;
    private String currencyCode;
    private String country;

    public SupplierFilterCriteria() {
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
