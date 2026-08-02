package com.materia.backend.contexts.requisition.domain.valueObjects;

import java.util.Objects;
import java.util.regex.Pattern;


public class RequisitionCode {
    
    // ============================================================
    // CONSTANTES
    // ============================================================
    
    private static final String PATTERN_STRING = "^REQ-[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);
    private static final String DEFAULT_PREFIX = "REQ";
    
    // ============================================================
    // ATTRIBUT
    // ============================================================
    
    private final String value;
    
    // ============================================================
    // CONSTRUCTEUR PRIVÉ
    // ============================================================
    
    private RequisitionCode(String value) {
        validate(value);
        this.value = value.trim();
    }
    
    // ============================================================
    // FACTORY METHODS
    // ============================================================
    
    /**
     * Crée un RequisitionCode à partir d'une valeur String
     */
    public static RequisitionCode of(String value) {
        return new RequisitionCode(value);
    }
    
    /**
     * Crée un RequisitionCode à partir d'un préfixe et d'un numéro
     */
    public static RequisitionCode fromPrefixAndNumber(String prefix, int number) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Le préfixe est obligatoire");
        }
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("Le numéro doit être entre 0 et 9999");
        }
        String id = prefix.trim().toUpperCase() + "-" + String.format("%04d", number);
        return new RequisitionCode(id);
    }
    
    /**
     * Crée un RequisitionCode par défaut
     */
    public static RequisitionCode createDefault() {
        return new RequisitionCode(DEFAULT_PREFIX + "-0001");
    }
    
    /**
     * Génère le prochain ID séquentiel
     */
    public static RequisitionCode generateNext(String current) {
        RequisitionCode id = RequisitionCode.of(current);
        int number = id.getNumberAsInt() + 1;
        return fromPrefixAndNumber(id.getPrefix(), number);
    }
    
    /**
     * Génère le prochain ID séquentiel
     */
    public static RequisitionCode generateNext(RequisitionCode current) {
        int number = current.getNumberAsInt() + 1;
        return fromPrefixAndNumber(current.getPrefix(), number);
    }
    
    /**
     * Valide un ID sans créer d'objet
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return PATTERN.matcher(value.trim()).matches();
    }
    
    // ============================================================
    // VALIDATION
    // ============================================================
    
    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("L'ID de la demande est obligatoire");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("L'ID de la demande ne peut pas être vide");
        }
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Format invalide pour l'ID de demande: '" + value + 
                "'. Format attendu: REQ-0000 (ex: REQ-0001)"
            );
        }
    }
    
    // ============================================================
    // MÉTHODES MÉTIER
    // ============================================================
    
    public String getPrefix() {
        return value.substring(0, 3);
    }
    
    public String getNumber() {
        return value.substring(4);
    }
    
    public int getNumberAsInt() {
        return Integer.parseInt(getNumber());
    }
    
    public RequisitionCode increment() {
        return generateNext(this);
    }
    
    public String getValue() {
        return value;
    }
    
    // ============================================================
    // EQUALS & HASHCODE
    // ============================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequisitionCode that = (RequisitionCode) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    // ============================================================
    // TOSTRING
    // ============================================================
    
    @Override
    public String toString() {
        return value;
    }
}
