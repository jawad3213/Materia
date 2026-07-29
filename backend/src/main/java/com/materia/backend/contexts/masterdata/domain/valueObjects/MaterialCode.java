package com.materia.backend.contexts.masterdata.domain.valueObjects;

import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;



import java.util.Objects;
import java.util.regex.Pattern;


public class MaterialCode {

    // ============================================================
    // CONSTANTES
    // ============================================================

    private static final String PATTERN_STRING = "^[A-Z]{3}-[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public static final String DEFAULT_PREFIX = "MAT";
    public static final String RAW_MATERIAL_PREFIX = "RMT";
    public static final String FINISHED_GOOD_PREFIX = "FGD";
    public static final String COMPONENT_PREFIX = "CMP";
    public static final String PACKAGING_PREFIX = "PKG";
    public static final String SPARE_PART_PREFIX = "SPR";

    // ============================================================
    // ATTRIBUT
    // ============================================================

    private final String value;

    // ============================================================
    // CONSTRUCTEUR PRIVÃ‰
    // ============================================================

    private MaterialCode(String value) {
        validate(value);
        this.value = value;
    }

    // ============================================================
    // FACTORY METHODS
    // ============================================================

    public static MaterialCode of(String value) {
        return new MaterialCode(value);
    }

    public static MaterialCode fromPrefixAndNumber(String prefix, int number) {
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException(
                    "Le numÃ©ro doit Ãªtre entre 0 et 9999"
            );
        }
        String code = prefix + "-" + String.format("%04d", number);
        return new MaterialCode(code);
    }

    public static MaterialCode generateNext(String current) {
        MaterialCode code = MaterialCode.of(current);
        int number = code.getNumberAsInt() + 1;
        return fromPrefixAndNumber(code.getPrefix(), number);
    }

    public static MaterialCode generateNext(MaterialCode current) {
        int number = current.getNumberAsInt() + 1;
        return fromPrefixAndNumber(current.getPrefix(), number);
    }

    public static MaterialCode createDefault() {
        return new MaterialCode("MAT-0001");
    }

    public static MaterialCode createForType(CategoryType type) {
        String prefix = switch (type) {
            case RAW_MATERIAL -> RAW_MATERIAL_PREFIX;
            case FINISHED_GOOD -> FINISHED_GOOD_PREFIX;
            case COMPONENT -> COMPONENT_PREFIX;
            case PACKAGING -> PACKAGING_PREFIX;
            case SPARE_PART -> SPARE_PART_PREFIX;
            default -> DEFAULT_PREFIX;
        };
        return fromPrefixAndNumber(prefix, 1);
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Le code du matÃ©riau est obligatoire"
            );
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Le code du matÃ©riau ne peut pas Ãªtre vide"
            );
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Format invalide pour le code matÃ©riau: " + value +
                            ". Format attendu: XXX-0000 (ex: MAT-0001)"
            );
        }
    }

    // ============================================================
    // MÃ‰THODES MÃ‰TIER
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

    public boolean startsWith(String prefix) {
        return value.startsWith(prefix);
    }

    public boolean isRawMaterial() {
        return startsWith(RAW_MATERIAL_PREFIX);
    }

    public boolean isFinishedGood() {
        return startsWith(FINISHED_GOOD_PREFIX);
    }

    public boolean isComponent() {
        return startsWith(COMPONENT_PREFIX);
    }

    public boolean isPackaging() {
        return startsWith(PACKAGING_PREFIX);
    }

    public boolean isSparePart() {
        return startsWith(SPARE_PART_PREFIX);
    }

    public MaterialCode increment() {
        return generateNext(this);
    }

    public MaterialCode withNewPrefix(String newPrefix) {
        return fromPrefixAndNumber(newPrefix, getNumberAsInt());
    }

    // ============================================================
    // GETTER
    // ============================================================

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
        MaterialCode that = (MaterialCode) o;
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
