package com.materia.backend.common.infrastructure.adapters.out.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO representing response payload from exchange rate providers.
 * Compatible with Open Exchange Rates, ExchangeRate-API (v4 and v6), and similar free standards.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateResponse {

    @JsonProperty("result")
    private String result;

    @JsonProperty("error-type")
    private String errorType;

    @JsonProperty("base_code")
    private String baseCode;

    @JsonProperty("base")
    private String base;

    @JsonProperty("target_code")
    private String targetCode;

    @JsonProperty("conversion_rate")
    private BigDecimal conversionRate;

    @JsonProperty("conversion_result")
    private BigDecimal conversionResult;

    @JsonProperty("time_last_update_unix")
    private Long timeLastUpdateUnix;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates = new HashMap<>();

    @JsonProperty("rates")
    private Map<String, BigDecimal> rates = new HashMap<>();

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(result);
    }

    public String getEffectiveBase() {
        if (baseCode != null && !baseCode.isBlank()) {
            return baseCode;
        }
        return base;
    }

    public Map<String, BigDecimal> getEffectiveRates() {
        if (conversionRates != null && !conversionRates.isEmpty()) {
            return conversionRates;
        }
        return rates != null ? rates : Map.of();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getTargetCode() {
        return targetCode;
    }

    public void setTargetCode(String targetCode) {
        this.targetCode = targetCode;
    }

    public BigDecimal getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }

    public BigDecimal getConversionResult() {
        return conversionResult;
    }

    public void setConversionResult(BigDecimal conversionResult) {
        this.conversionResult = conversionResult;
    }

    public Long getTimeLastUpdateUnix() {
        return timeLastUpdateUnix;
    }

    public void setTimeLastUpdateUnix(Long timeLastUpdateUnix) {
        this.timeLastUpdateUnix = timeLastUpdateUnix;
    }

    public Map<String, BigDecimal> getConversionRates() {
        return conversionRates;
    }

    public void setConversionRates(Map<String, BigDecimal> conversionRates) {
        this.conversionRates = conversionRates != null ? conversionRates : new HashMap<>();
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates != null ? rates : new HashMap<>();
    }
}
