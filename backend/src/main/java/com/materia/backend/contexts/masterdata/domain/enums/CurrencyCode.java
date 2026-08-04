package com.materia.backend.contexts.masterData.domain.enums;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Currency Codes
 * Simplified version - MAD, EUR, USD
 *
 * @author SAP MM Team
 * @version 1.0
 */
public enum CurrencyCode {

    // ============================================================
    // AVAILABLE CURRENCIES
    // ============================================================

    MAD("MAD", "Moroccan Dirham", "DH", 2),
    EUR("EUR", "Euro", "\u20AC", 2),
    USD("USD", "US Dollar", "$", 2);

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    private final String code;
    private final String label;
    private final String symbol;
    private final int decimalPlaces;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    CurrencyCode(String code, String label, String symbol, int decimalPlaces) {
        this.code = code;
        this.label = label;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getSymbol() { return symbol; }
    public int getDecimalPlaces() { return decimalPlaces; }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    /**
     * Retrieves a currency by its code
     */
    public static CurrencyCode fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Currency code is required");
        }
        for (CurrencyCode currency : values()) {
            if (currency.code.equals(code.toUpperCase())) { return currency; }
        }
        throw new IllegalArgumentException("Unknown currency: " + code + ". Allowed values: MAD, EUR, USD");
    }

    /**
     * Retrieves a currency by its label
     */
    public static CurrencyCode fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Label is required");
        }
        for (CurrencyCode currency : values()) {
            if (currency.label.equalsIgnoreCase(label)) { return currency; }
        }
        throw new IllegalArgumentException("Unknown currency: " + label);
    }

    /**
     * Checks if a code exists
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) { return false; }
        return Arrays.stream(values()).anyMatch(currency -> currency.code.equals(code.toUpperCase()));
    }

    /**
     * Retrieves all codes
     */
    public static List<String> getCodes() {
        return Arrays.stream(values()).map(CurrencyCode::getCode).collect(Collectors.toList());
    }

    /**
     * Retrieves all labels
     */
    public static List<String> getLabels() {
        return Arrays.stream(values()).map(CurrencyCode::getLabel).collect(Collectors.toList());
    }

    /**
     * Retrieves the default currency (EUR)
     */
    public static CurrencyCode getDefault() { return EUR; }

    /**
     * Formats an amount with the symbol
     * Example: DH 850.00
     */
    public String format(BigDecimal amount) {
        if (amount == null) { return symbol + " 0.00"; }
        String pattern = "%." + decimalPlaces + "f";
        return symbol + " " + String.format(pattern, amount);
    }

    /**
     * Formats an amount with the code
     * Example: MAD 850.00
     */
    public String formatWithCode(BigDecimal amount) {
        if (amount == null) { return code + " 0.00"; }
        String pattern = "%." + decimalPlaces + "f";
        return code + " " + String.format(pattern, amount);
    }

    /**
     * Formats an amount without symbol
     * Example: 850.00
     */
    public String formatPlain(BigDecimal amount) {
        if (amount == null) { return "0.00"; }
        String pattern = "%." + decimalPlaces + "f";
        return String.format(pattern, amount);
    }

    /**
     * Checks if this is the Moroccan Dirham
     */
    public boolean isMAD() { return this == MAD; }

    /**
     * Checks if this is the Euro
     */
    public boolean isEuro() { return this == EUR; }

    /**
     * Checks if this is the US Dollar
     */
    public boolean isUSD() { return this == USD; }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", code, symbol, label);
    }
}
