package com.materia.backend.contexts.masterData.domain.enums;

/**

 */
public enum StockStatus {
    
    IN_STOCK("IN_STOCK", "En stock", "🟢", "Niveau de stock normal"),
    REORDER_NEEDED("REORDER_NEEDED", "Réapprovisionnement", "🟡", "Stock en dessous du point de réapprovisionnement"),
    CRITICAL("CRITICAL", "Critique", "🟠", "Stock en dessous du stock de sécurité"),
    OUT_OF_STOCK("OUT_OF_STOCK", "Rupture", "🔴", "Stock épuisé");
    
    private final String code;
    private final String label;
    private final String icon;
    private final String description;
    
    StockStatus(String code, String label, String icon, String description) {
        this.code = code;
        this.label = label;
        this.icon = icon;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
    
    public static StockStatus fromCode(String code) {
        for (StockStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Statut inconnu : " + code);
    }
}
