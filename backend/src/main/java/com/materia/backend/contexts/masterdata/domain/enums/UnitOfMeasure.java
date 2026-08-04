package com.materia.backend.contexts.masterData.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Standardized Units of Measure
 * ISO 80000 - Quantities and Units
 *
 * Location: masterdata/domain/enums/UnitOfMeasure.java
 */
public enum UnitOfMeasure {

    // ============================================================
    // MASS / WEIGHT
    // ============================================================
    KG("KG", "Kilogram", "Mass"),
    G("G", "Gram", "Mass"),
    T("T", "Tonne", "Mass"),
    MG("MG", "Milligram", "Mass"),
    LB("LB", "Pound", "Mass"),
    OZ("OZ", "Ounce", "Mass"),

    // ============================================================
    // VOLUME / LIQUIDS
    // ============================================================
    L("L", "Liter", "Volume"),
    ML("ML", "Milliliter", "Volume"),
    M3("M3", "Cubic Meter", "Volume"),
    DM3("DM3", "Cubic Decimeter", "Volume"),
    CM3("CM3", "Cubic Centimeter", "Volume"),
    GAL("GAL", "Gallon", "Volume"),
    FT3("FT3", "Cubic Foot", "Volume"),

    // ============================================================
    // LENGTH / DISTANCE
    // ============================================================
    M("M", "Meter", "Length"),
    CM("CM", "Centimeter", "Length"),
    MM("MM", "Millimeter", "Length"),
    KM("KM", "Kilometer", "Length"),
    IN("IN", "Inch", "Length"),
    FT("FT", "Foot", "Length"),
    YD("YD", "Yard", "Length"),

    // ============================================================
    // AREA
    // ============================================================
    M2("M2", "Square Meter", "Area"),
    CM2("CM2", "Square Centimeter", "Area"),
    MM2("MM2", "Square Millimeter", "Area"),
    HA("HA", "Hectare", "Area"),
    ACRE("ACRE", "Acre", "Area"),

    // ============================================================
    // COUNT / UNITS
    // ============================================================
    PCE("PCE", "Piece", "Count"),
    BOX("BOX", "Box", "Count"),
    CART("CART", "Carton", "Count"),
    PACK("PACK", "Pack", "Count"),
    SET("SET", "Set", "Count"),
    PAL("PAL", "Pallet", "Count"),
    DRUM("DRUM", "Drum", "Count"),
    ROLL("ROLL", "Roll", "Count"),
    SHEET("SHEET", "Sheet", "Count"),
    REEL("REEL", "Reel", "Count"),

    // ============================================================
    // TIME
    // ============================================================
    HOUR("HOUR", "Hour", "Time"),
    DAY("DAY", "Day", "Time"),
    WEEK("WEEK", "Week", "Time"),
    MONTH("MONTH", "Month", "Time"),

    // ============================================================
    // ENERGY / POWER
    // ============================================================
    KWH("KWH", "Kilowatt-hour", "Energy"),
    KW("KW", "Kilowatt", "Power"),
    HP("HP", "Horsepower", "Power"),

    // ============================================================
    // TEMPERATURE
    // ============================================================
    C("C", "Celsius", "Temperature"),
    F("F", "Fahrenheit", "Temperature"),
    K("K", "Kelvin", "Temperature"),

    // ============================================================
    // PRESSURE
    // ============================================================
    BAR("BAR", "Bar", "Pressure"),
    PSI("PSI", "Pound per square inch", "Pressure"),
    PA("PA", "Pascal", "Pressure"),
    ATM("ATM", "Atmosphere", "Pressure"),

    // ============================================================
    // OTHER
    // ============================================================
    NONE("NONE", "No Unit", "None");

    // ============================================================
    // ATTRIBUTES
    // ============================================================

    private final String code;
    private final String label;
    private final String category;

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    UnitOfMeasure(String code, String label, String category) {
        this.code = code;
        this.label = label;
        this.category = category;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getCategory() { return category; }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    public static UnitOfMeasure fromCode(String code) {
        if (code == null) return null;
        for (UnitOfMeasure unit : values()) {
            if (unit.getCode().equals(code)) { return unit; }
        }
        throw new IllegalArgumentException("Unknown unit: " + code);
    }

    public static UnitOfMeasure fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit of measure is required");
        }

        String normalized = value.trim();
        try {
            return UnitOfMeasure.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // Fall through to code and label matching.
        }

        for (UnitOfMeasure unit : values()) {
            if (unit.code.equalsIgnoreCase(normalized) || unit.label.equalsIgnoreCase(normalized)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("Unknown unit: " + value);
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (UnitOfMeasure unit : values()) {
            if (unit.getCode().equals(code)) { return true; }
        }
        return false;
    }

    public static List<UnitOfMeasure> getByCategory(String category) {
        return Arrays.stream(values()).filter(u -> u.getCategory().equals(category)).collect(Collectors.toList());
    }

    public static List<UnitOfMeasure> getMassUnits() { return getByCategory("Mass"); }
    public static List<UnitOfMeasure> getVolumeUnits() { return getByCategory("Volume"); }
    public static List<UnitOfMeasure> getCountUnits() { return getByCategory("Count"); }

    @Override
    public String toString() { return code + " - " + label; }
}
