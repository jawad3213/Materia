package com.materia.backend.contexts.masterdata.domain.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UnitÃ©s de mesure standardisÃ©es
 * ISO 80000 - Grandeurs et unitÃ©s
 *
 * ðŸ“ Position: masterData-service/domain/enums/UnitOfMeasure.java
 */
public enum UnitOfMeasure {

    // ============================================================
    // MASSE / POIDS
    // ============================================================
    KG("KG", "Kilogramme", "Mass"),
    G("G", "Gramme", "Mass"),
    T("T", "Tonne", "Mass"),
    MG("MG", "Milligramme", "Mass"),
    LB("LB", "Livre", "Mass"),
    OZ("OZ", "Once", "Mass"),

    // ============================================================
    // VOLUME / LIQUIDES
    // ============================================================
    L("L", "Litre", "Volume"),
    ML("ML", "Millilitre", "Volume"),
    M3("M3", "MÃ¨tre cube", "Volume"),
    DM3("DM3", "DÃ©cimÃ¨tre cube", "Volume"),
    CM3("CM3", "CentimÃ¨tre cube", "Volume"),
    GAL("GAL", "Gallon", "Volume"),
    FT3("FT3", "Pied cube", "Volume"),

    // ============================================================
    // LONGUEUR / DISTANCE
    // ============================================================
    M("M", "MÃ¨tre", "Length"),
    CM("CM", "CentimÃ¨tre", "Length"),
    MM("MM", "MillimÃ¨tre", "Length"),
    KM("KM", "KilomÃ¨tre", "Length"),
    IN("IN", "Inch", "Length"),
    FT("FT", "Pied", "Length"),
    YD("YD", "Yard", "Length"),

    // ============================================================
    // SURFACE
    // ============================================================
    M2("M2", "MÃ¨tre carrÃ©", "Area"),
    CM2("CM2", "CentimÃ¨tre carrÃ©", "Area"),
    MM2("MM2", "MillimÃ¨tre carrÃ©", "Area"),
    HA("HA", "Hectare", "Area"),
    ACRE("ACRE", "Acre", "Area"),

    // ============================================================
    // COMPTAGE / UNITÃ‰S
    // ============================================================
    PCE("PCE", "PiÃ¨ce", "Count"),
    BOX("BOX", "BoÃ®te", "Count"),
    CART("CART", "Carton", "Count"),
    PACK("PACK", "Paquet", "Count"),
    SET("SET", "Set", "Count"),
    PAL("PAL", "Palette", "Count"),
    DRUM("DRUM", "FÃ»t", "Count"),
    ROLL("ROLL", "Rouleau", "Count"),
    SHEET("SHEET", "Feuille", "Count"),
    REEL("REEL", "Bobine", "Count"),

    // ============================================================
    // TEMPS
    // ============================================================
    HOUR("HOUR", "Heure", "Time"),
    DAY("DAY", "Jour", "Time"),
    WEEK("WEEK", "Semaine", "Time"),
    MONTH("MONTH", "Mois", "Time"),

    // ============================================================
    // Ã‰NERGIE / PUISSANCE
    // ============================================================
    KWH("KWH", "Kilowatt-heure", "Energy"),
    KW("KW", "Kilowatt", "Power"),
    HP("HP", "Cheval-vapeur", "Power"),

    // ============================================================
    // TEMPÃ‰RATURE
    // ============================================================
    C("C", "Celsius", "Temperature"),
    F("F", "Fahrenheit", "Temperature"),
    K("K", "Kelvin", "Temperature"),

    // ============================================================
    // PRESSION
    // ============================================================
    BAR("BAR", "Bar", "Pressure"),
    PSI("PSI", "Pound per square inch", "Pressure"),
    PA("PA", "Pascal", "Pressure"),
    ATM("ATM", "AtmosphÃ¨re", "Pressure"),

    // ============================================================
    // AUTRES
    // ============================================================
    NONE("NONE", "Aucune unitÃ©", "None");

    // ============================================================
    // ATTRIBUTS
    // ============================================================

    private final String code;
    private final String label;
    private final String category;

    // ============================================================
    // CONSTRUCTEUR
    // ============================================================

    UnitOfMeasure(String code, String label, String category) {
        this.code = code;
        this.label = label;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    // ============================================================
    // MÃ‰THODES UTILITAIRES
    // ============================================================

    public static UnitOfMeasure fromCode(String code) {
        if (code == null) return null;
        for (UnitOfMeasure unit : values()) {
            if (unit.getCode().equals(code)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("UnitÃ© inconnue: " + code);
    }

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (UnitOfMeasure unit : values()) {
            if (unit.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public static List<UnitOfMeasure> getByCategory(String category) {
        return Arrays.stream(values())
                .filter(u -> u.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public static List<UnitOfMeasure> getMassUnits() {
        return getByCategory("Mass");
    }

    public static List<UnitOfMeasure> getVolumeUnits() {
        return getByCategory("Volume");
    }

    public static List<UnitOfMeasure> getCountUnits() {
        return getByCategory("Count");
    }

    @Override
    public String toString() {
        return code + " - " + label;
    }
}
