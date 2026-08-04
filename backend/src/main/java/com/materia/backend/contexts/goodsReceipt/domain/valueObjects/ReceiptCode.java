package com.materia.backend.contexts.goodsReceipt.domain.valueObjects;


import java.util.Objects;
import java.util.regex.Pattern;

public class ReceiptCode {
    
    private static final String PATTERN_STRING = "^GR-[0-9]{4}$";
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
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Le préfixe est obligatoire");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Le numéro doit être entre 0 et 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + String.format("%04d", number);
        return new ReceiptCode(id);
    }
    
    public static ReceiptCode createDefault() {
        return new ReceiptCode(DEFAULT_PREFIX + "-0001");
    }
    
    public static ReceiptCode generateNext(String current) {
        ReceiptCode id = ReceiptCode.of(current);
        int number = id.getNumberAsInt() + 1;
        return fromPrefixAndNumber(id.getPrefix(), number);
    }
    
    public static ReceiptCode generateNext(ReceiptCode current) {
        int number = current.getNumberAsInt() + 1;
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
                "'. Format attendu: GR-0000 (ex: GR-0001)"
            );
        }
    }
    
    public String getPrefix() {
        return value.substring(0, 2);
    }
    
    public String getNumber() {
        return value.substring(3);
    }
    
    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
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