package com.materia.backend.contexts.masterData.domain.valueObjects;

import com.materia.backend.contexts.masterData.domain.enums.MaterialType;

import java.util.Objects;
import java.util.regex.Pattern;


public class MaterialCode {

    // ============================================================
    // CONSTANTS
    // ============================================================

    private static final String PATTERN_STRING = "^[A-Z]{3}-[0-9]{4}$";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public static final String DEFAULT_PREFIX = "MAT";
    public static final String RAW_MATERIAL_PREFIX = "RMT";
    public static final String FINISHED_GOOD_PREFIX = "FGD";
    public static final String COMPONENT_PREFIX = "CMP";
    public static final String PACKAGING_PREFIX = "PKG";
    public static final String SPARE_PART_PREFIX = "SPR";
    public static final String CONSUMABLE_PREFIX = "CNS";
    public static final String SERVICE_PREFIX = "SRV";
    public static final String TOOL_PREFIX = "TOL";
    public static final String CHEMICAL_PREFIX = "CHM";
    public static final String ELECTRONIC_PREFIX = "ELC";

    // ============================================================
    // ATTRIBUTE
    // ============================================================

    private final String value;

    // ============================================================
    // PRIVATE CONSTRUCTOR
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
            throw new IllegalArgumentException("Number must be between 0 and 9999");
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

    public static MaterialCode createForType(MaterialType type) {
        if (type == null) {
            return createDefault();
        }
        return fromPrefixAndNumber(type.getPrefix(), 1);
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private void validate(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Material code is required");
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Material code cannot be empty");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Invalid format for material code: " + value +
                            ". Expected format: XXX-0000 (e.g. MAT-0001)"
            );
        }
    }

    // ============================================================
    // BUSINESS METHODS
    // ============================================================

    public String getPrefix() { return value.substring(0, 3); }
    public String getNumber() { return value.substring(4); }
    public int getNumberAsInt() { return Integer.parseInt(getNumber()); }
    public boolean startsWith(String prefix) { return value.startsWith(prefix); }

    public boolean isRawMaterial() { return startsWith(RAW_MATERIAL_PREFIX); }
    public boolean isFinishedGood() { return startsWith(FINISHED_GOOD_PREFIX); }
    public boolean isComponent() { return startsWith(COMPONENT_PREFIX); }
    public boolean isPackaging() { return startsWith(PACKAGING_PREFIX); }
    public boolean isSparePart() { return startsWith(SPARE_PART_PREFIX); }
    public boolean isConsumable() { return startsWith(CONSUMABLE_PREFIX); }
    public boolean isService() { return startsWith(SERVICE_PREFIX); }
    public boolean isTool() { return startsWith(TOOL_PREFIX); }
    public boolean isChemical() { return startsWith(CHEMICAL_PREFIX); }
    public boolean isElectronic() { return startsWith(ELECTRONIC_PREFIX); }

    public MaterialCode increment() { return generateNext(this); }

    public MaterialCode withNewPrefix(String newPrefix) {
        return fromPrefixAndNumber(newPrefix, getNumberAsInt());
    }

    // ============================================================
    // GETTER
    // ============================================================

    public String getValue() { return value; }

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
    public int hashCode() { return Objects.hash(value); }

    // ============================================================
    // TOSTRING
    // ============================================================

    @Override
    public String toString() { return value; }
}
