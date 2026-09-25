package com.materia.backend.contexts.goodsReceipt.domain.valueObjects;


import java.util.Objects;
import java.util.regex.Pattern;

public class ReceiptCode {
    
    private static final String PATTERN_STRING = "^(?i)GR-(?:[0-9]{4}-)?[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static final String DEFAULT_PREFIX = "GR";
    
    private final String value;
    
    private ReceiptCode(String value) {
        validate(value);
        this.value = value.trim();
    }
    
    public static ReceiptCode of(String value) {
        return new ReceiptCode(value);
    }
    
    public static ReceiptCode fromPrefixAndNumber(String prefix, int number) {
        return fromPrefixYearAndNumber(prefix, java.time.Year.now().getValue(), number);
    }

    public static ReceiptCode fromPrefixYearAndNumber(String prefix, int year, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Le préfixe est obligatoire");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Le numéro doit être entre 0 et 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + year + "-" + String.format("%04d", number);
        return new ReceiptCode(id);
    }
    
    public static ReceiptCode createDefault() {
        return fromPrefixAndNumber(DEFAULT_PREFIX, 1);
    }
    
    public static ReceiptCode generateNext(String current) {
        return generateNext(ReceiptCode.of(current));
    }
    
    public static ReceiptCode generateNext(ReceiptCode current) {
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
            throw new IllegalArgumentException("Le code de réception est obligatoire");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Le code de réception ne peut pas être vide");
        }
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Format invalide pour le code de réception: '" + value + 
                "'. Format attendu: GR-YYYY-0001 (ex: GR-2026-0001) ou legacy GR-0001"
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
    
    public ReceiptCode increment() {
        return generateNext(this);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptCode that = (ReceiptCode) o;
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