package com.materia.backend.contexts.invoice.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Invoice Code Value Object
 * Code unique d'une facture
 * Format: INV-0000 (ex: INV-0001)
 * 
 * @author SAP MM Team
 * @version 1.0
 */
public class InvoiceCode {
    
    private static final String PATTERN_STRING = "^(?i)INV-(?:[0-9]{4}-)?[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static final String DEFAULT_PREFIX = "INV";
    
    private final String value;
    
    private InvoiceCode(String value) {
        validate(value);
        this.value = value.trim();
    }
    
    public static InvoiceCode of(String value) {
        return new InvoiceCode(value);
    }
    
    public static InvoiceCode fromPrefixAndNumber(String prefix, int number) {
        return fromPrefixYearAndNumber(prefix, java.time.Year.now().getValue(), number);
    }

    public static InvoiceCode fromPrefixYearAndNumber(String prefix, int year, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Le préfixe est obligatoire");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Le numéro doit être entre 0 et 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + year + "-" + String.format("%04d", number);
        return new InvoiceCode(id);
    }
    
    public static InvoiceCode createDefault() {
        return fromPrefixAndNumber(DEFAULT_PREFIX, 1);
    }
    
    public static InvoiceCode generateNext(String current) {
        return generateNext(InvoiceCode.of(current));
    }
    
    public static InvoiceCode generateNext(InvoiceCode current) {
        int number = current.getNumberAsInt() + 1;
        int year = current.getYear();
        if (year != -1) {
            return fromPrefixYearAndNumber(current.getPrefix(), year, number);
        }
        return fromPrefixAndNumber(current.getPrefix(), number);
    }
    
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(value.trim()).matches();
    }
    
    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Le code de facture est obligatoire");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Le code de facture ne peut pas être vide");
        }
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Format invalide pour le code de facture: '" + value + 
                "'. Format attendu: INV-YYYY-0001 (ex: INV-2026-0001) ou legacy INV-0001"
            );
        }
    }
    
    public String getPrefix() {
        int hyphenIndex = value.indexOf('-');
        return hyphenIndex != -1 ? value.substring(0, hyphenIndex) : value;
    }
    
    public String getNumber() {
        int hyphenIndex = value.lastIndexOf('-');
        return hyphenIndex != -1 ? value.substring(hyphenIndex + 1) : value;
    }
    
    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
    }

    public int getYear() {
        String[] parts = value.split("-");
        if (parts.length == 3) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    public InvoiceCode increment() {
        return generateNext(this);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvoiceCode that = (InvoiceCode) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
