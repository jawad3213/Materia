package com.materia.backend.contexts.payement.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;


public class PaymentCode {
    
    private static final String PATTERN_STRING = "^PAY-[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static final String DEFAULT_PREFIX = "PAY";
    
    private final String value;
    
    private PaymentCode(String value) {
        validate(value);
        this.value = value.trim();
    }
    
    public static PaymentCode of(String value) {
        return new PaymentCode(value);
    }
    
    public static PaymentCode fromPrefixAndNumber(String prefix, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Le préfixe est obligatoire");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Le numéro doit être entre 0 et 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + String.format("%04d", number);
        return new PaymentCode(id);
    }
    
    public static PaymentCode createDefault() {
        return new PaymentCode(DEFAULT_PREFIX + "-0001");
    }
    
    public static PaymentCode generateNext(String current) {
        PaymentCode id = PaymentCode.of(current);
        int number = id.getNumberAsInt() + 1;
        return fromPrefixAndNumber(id.getPrefix(), number);
    }
    
    public static PaymentCode generateNext(PaymentCode current) {
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
            throw new IllegalArgumentException("Le code de paiement est obligatoire");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Le code de paiement ne peut pas être vide");
        }
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Format invalide pour le code de paiement: '" + value + 
                "'. Format attendu: PAY-0000 (ex: PAY-0001)"
            );
        }
    }
    
    public String getPrefix() {
        return value.substring(0, 3);
    }
    
    public String getNumber() {
        return value.substring(4);
    }
    
    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
    }
    
    public PaymentCode increment() {
        return generateNext(this);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentCode that = (PaymentCode) o;
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
