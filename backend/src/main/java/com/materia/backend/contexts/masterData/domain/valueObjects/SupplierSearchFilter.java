package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.common.domain.enums.CurrencyCode;

/**
 * Value object representing criteria for searching/filtering suppliers.
 */
public class SupplierSearchFilter {

    // ---- Search fields ----
    private final String code;
    private final String name;
    private final String description;
    private final String contactPerson;
    private final String contactEmail;
    private final String fullAddress;

    // ---- Filter fields ----
    private final String status;
    private final CurrencyCode currencyCode;
    private final String country;

    private SupplierSearchFilter(Builder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.description = builder.description;
        this.contactPerson = builder.contactPerson;
        this.contactEmail = builder.contactEmail;
        this.fullAddress = builder.fullAddress;
        this.status = builder.status;
        this.currencyCode = builder.currencyCode;
        this.country = builder.country;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getContactPerson() { return contactPerson; }
    public String getContactEmail() { return contactEmail; }
    public String getFullAddress() { return fullAddress; }
    public String getStatus() { return status; }
    public CurrencyCode getCurrencyCode() { return currencyCode; }
    public String getCountry() { return country; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String code;
        private String name;
        private String description;
        private String contactPerson;
        private String contactEmail;
        private String fullAddress;
        private String status;
        private CurrencyCode currencyCode;
        private String country;

        public Builder code(String code) { this.code = code; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder contactPerson(String contactPerson) { this.contactPerson = contactPerson; return this; }
        public Builder contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
        public Builder fullAddress(String fullAddress) { this.fullAddress = fullAddress; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder currencyCode(CurrencyCode currencyCode) { this.currencyCode = currencyCode; return this; }
        public Builder country(String country) { this.country = country; return this; }

        public SupplierSearchFilter build() {
            return new SupplierSearchFilter(this);
        }
    }
}
