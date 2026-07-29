package com.materia.backend.contexts.masterdata.domain.enums;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Devises (Currency Codes)
 * Version simplifiÃ©e - MAD, EUR, USD
 *
 * @author SAP MM Team
 * @version 1.0
 */
public enum CurrencyCode {

    // ============================================================
    // DEVISE DISPONIBLES
    // ============================================================

    MAD("MAD", "Dirham Marocain", "DH", 2),
    EUR("EUR", "Euro", "â‚¬", 2),
    USD("USD", "Dollar US", "$", 2);

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    private final String code;
    private final String label;
    private final String symbol;
    private final int decimalPlaces;

    // ============================================================
    // CONSTRUCTEUR
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

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    // ============================================================
    // MÃ‰THODES UTILITAIRES
    // ============================================================

    /**
     * RÃ©cupÃ¨re une devise par son code
     */
    public static CurrencyCode fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Le code devise est obligatoire");
        }
        for (CurrencyCode currency : values()) {
            if (currency.code.equals(code.toUpperCase())) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Devise inconnue : " + code +
                ". Valeurs autorisÃ©es: MAD, EUR, USD");
    }

    /**
     * RÃ©cupÃ¨re une devise par son label
     */
    public static CurrencyCode fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Le label est obligatoire");
        }
        for (CurrencyCode currency : values()) {
            if (currency.label.equalsIgnoreCase(label)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Devise inconnue : " + label);
    }

    /**
     * VÃ©rifie si un code existe
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values())
                .anyMatch(currency -> currency.code.equals(code.toUpperCase()));
    }

    /**
     * RÃ©cupÃ¨re tous les codes
     */
    public static List<String> getCodes() {
        return Arrays.stream(values())
                .map(CurrencyCode::getCode)
                .collect(Collectors.toList());
    }

    /**
     * RÃ©cupÃ¨re tous les labels
     */
    public static List<String> getLabels() {
        return Arrays.stream(values())
                .map(CurrencyCode::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * RÃ©cupÃ¨re la devise par dÃ©faut (EUR)
     */
    public static CurrencyCode getDefault() {
        return EUR;
    }

    /**
     * Formate un montant avec le symbole
     * Exemple: DH 850.00
     */
    public String format(BigDecimal amount) {
        if (amount == null) {
            return symbol + " 0.00";
        }
        String pattern = "%." + decimalPlaces + "f";
        return symbol + " " + String.format(pattern, amount);
    }

    /**
     * Formate un montant avec le code
     * Exemple: MAD 850.00
     */
    public String formatWithCode(BigDecimal amount) {
        if (amount == null) {
            return code + " 0.00";
        }
        String pattern = "%." + decimalPlaces + "f";
        return code + " " + String.format(pattern, amount);
    }

    /**
     * Formate un montant sans symbole
     * Exemple: 850.00
     */
    public String formatPlain(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        String pattern = "%." + decimalPlaces + "f";
        return String.format(pattern, amount);
    }

    /**
     * VÃ©rifie si c'est le Dirham Marocain
     */
    public boolean isMAD() {
        return this == MAD;
    }

    /**
     * VÃ©rifie si c'est l'Euro
     */
    public boolean isEuro() {
        return this == EUR;
    }

    /**
     * VÃ©rifie si c'est le Dollar US
     */
    public boolean isUSD() {
        return this == USD;
    }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", code, symbol, label);
    }
}
