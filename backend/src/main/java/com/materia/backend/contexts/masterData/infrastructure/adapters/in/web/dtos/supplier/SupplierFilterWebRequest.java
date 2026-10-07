package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier;
import com.materia.backend.common.domain.enums.CurrencyCode;

/**
 * Dedicated request payload for UI filtering (Status, Currency, Country).
 */
public class SupplierFilterWebRequest {

    private String status;
    private String currencyCode;
    private String country;

    public SupplierFilterWebRequest() {
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
