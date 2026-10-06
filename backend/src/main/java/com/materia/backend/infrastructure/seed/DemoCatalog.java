package com.materia.backend.infrastructure.seed;

import com.materia.backend.common.domain.enums.CurrencyCode;
import com.materia.backend.contexts.auth.domain.enums.Role;
import com.materia.backend.contexts.masterData.domain.enums.MaterialCategoryType;
import com.materia.backend.contexts.masterData.domain.enums.MaterialStatus;
import com.materia.backend.contexts.masterData.domain.enums.MaterialType;
import com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure;

import java.util.List;

import static com.materia.backend.contexts.masterData.domain.enums.MaterialType.*;
import static com.materia.backend.contexts.masterData.domain.enums.UnitOfMeasure.*;

/**
 * Reference data of the demo company (an industrial plant near Casablanca): its staff, material categories,
 * suppliers and catalog. Every name is fictitious.
 */
final class DemoCatalog {

    private DemoCatalog() {
    }

    /** Where a material's stock should end, so that the dashboard has stock alerts of every severity. */
    enum StockTarget { NORMAL, REORDER, CRITICAL, OUT }

    record PersonSpec(String firstName, String lastName, String email, Role role, String department, String phone) {
        String fullName() {
            return firstName + " " + lastName;
        }
    }

    record CategorySpec(String key, String name, String description, MaterialCategoryType type, String parentKey) {
    }

    record SupplierSpec(String key, String name, String description, String contactPerson, String email, String phone,
                        String address, String city, String country, String postalCode, List<String> paymentTerms,
                        int paymentDelay, CurrencyCode currency, boolean active, int orderWeight,
                        double onTimeRate, double defectRate, int minLeadDays, int maxLeadDays) {
    }

    record MaterialSpec(String name, String description, MaterialType type, UnitOfMeasure unit, String categoryKey,
                        String supplierKey, String price, int reorderPoint, int safetyStock, int maximumStock,
                        int orderQuantity, int monthlyUsage, MaterialStatus status, StockTarget target) {

        MaterialSpec(String name, String description, MaterialType type, UnitOfMeasure unit, String categoryKey,
                     String supplierKey, String price, int reorderPoint, int safetyStock, int maximumStock,
                     int orderQuantity, int monthlyUsage) {
            this(name, description, type, unit, categoryKey, supplierKey, price, reorderPoint, safetyStock, maximumStock,
                    orderQuantity, monthlyUsage, MaterialStatus.ACTIVE, StockTarget.NORMAL);
        }

        MaterialSpec target(StockTarget newTarget) {
            return new MaterialSpec(name, description, type, unit, categoryKey, supplierKey, price, reorderPoint,
                    safetyStock, maximumStock, orderQuantity, monthlyUsage, status, newTarget);
        }

        MaterialSpec status(MaterialStatus newStatus) {
            return new MaterialSpec(name, description, type, unit, categoryKey, supplierKey, price, reorderPoint,
                    safetyStock, maximumStock, orderQuantity, monthlyUsage, newStatus, target);
        }
    }

    /** Admins approve requisitions and pay invoices; purchasers order; receivers receive goods. */
    static final List<PersonSpec> PEOPLE = List.of(
            new PersonSpec("Youssef", "Benali", "y.benali@materia.ma", Role.ADMIN, "Management", "+212 661-204517"),
            new PersonSpec("Salma", "Idrissi", "s.idrissi@materia.ma", Role.ADMIN, "Finance", "+212 662-873140"),
            new PersonSpec("Karim", "El Amrani", "k.elamrani@materia.ma", Role.PURCHASER, "Procurement", "+212 663-519208"),
            new PersonSpec("Nadia", "Tazi", "n.tazi@materia.ma", Role.PURCHASER, "Procurement", "+212 664-092385"),
            new PersonSpec("Omar", "Chraibi", "o.chraibi@materia.ma", Role.PURCHASER, "Production", "+212 665-731964"),
            new PersonSpec("Hamza", "Ouazzani", "h.ouazzani@materia.ma", Role.RECEIVER, "Logistics", "+212 666-458012"),
            new PersonSpec("Imane", "Berrada", "i.berrada@materia.ma", Role.RECEIVER, "Warehouse", "+212 667-260853"),
            new PersonSpec("Rachid", "Alaoui", "r.alaoui@materia.ma", Role.RECEIVER, "Warehouse", "+212 668-617349"));

    static final List<CategorySpec> CATEGORIES = List.of(
            new CategorySpec("RAW", "Raw Materials", "Materials transformed by the production lines", MaterialCategoryType.RAW_MATERIAL_CAT, null),
            new CategorySpec("METAL", "Steel & Metals", "Sheets, bars, tubes and coils", MaterialCategoryType.RAW_MATERIAL_CAT, "RAW"),
            new CategorySpec("PLASTIC", "Plastics & Polymers", "Granules, sheets and masterbatches", MaterialCategoryType.RAW_MATERIAL_CAT, "RAW"),
            new CategorySpec("ELEC", "Electrical Components", "Cables, protection, automation and lighting", MaterialCategoryType.ELECTRONIC_CAT, null),
            new CategorySpec("PACK", "Packaging", "Boxes, pallets, films and strapping", MaterialCategoryType.PACKAGING_CAT, null),
            new CategorySpec("CHEM", "Chemicals & Lubricants", "Paints, oils, degreasers and fluids", MaterialCategoryType.CHEMICAL_CAT, null),
            new CategorySpec("SPARE", "Spare Parts", "Maintenance parts for production equipment", MaterialCategoryType.SPARE_PART_CAT, null),
            new CategorySpec("PPE", "Consumables & PPE", "Welding, abrasives and personal protective equipment", MaterialCategoryType.CONSUMABLE_CAT, null),
            new CategorySpec("TOOL", "Tools & Instruments", "Hand tools, power tools and measuring instruments", MaterialCategoryType.TOOL_CAT, null));

    static final List<SupplierSpec> SUPPLIERS = List.of(
            new SupplierSpec("ATLAS", "Atlas Steel Industries", "Flat and long steel products, cut to length",
                    "Mehdi Fassi", "sales@atlas-steel.ma", "+212 522-348790", "Km 12, Route de Rabat, Ain Sebaa",
                    "Casablanca", "Morocco", "20250", List.of("Bank transfer", "60 days end of month"), 60,
                    CurrencyCode.MAD, true, 18, 0.82, 0.10, 6, 14),
            new SupplierSpec("POLY", "Maghreb Polymers SARL", "Distributor of thermoplastic resins and masterbatches",
                    "Laila Bennani", "orders@maghreb-polymers.ma", "+212 539-394512", "Zone Franche Ilot 54, Gzenaya",
                    "Tangier", "Morocco", "90100", List.of("Bank transfer", "45 days"), 45,
                    CurrencyCode.MAD, true, 13, 0.88, 0.07, 5, 12),
            new SupplierSpec("SOFELEC", "Sofelec Maroc", "Electrical equipment wholesaler",
                    "Anas Kettani", "commercial@sofelec.ma", "+212 537-716630", "18 Rue Ibn Khaldoun, Agdal",
                    "Rabat", "Morocco", "10090", List.of("Bank transfer", "30 days"), 30,
                    CurrencyCode.MAD, true, 11, 0.90, 0.04, 3, 8),
            new SupplierSpec("NORDPACK", "Emballages du Nord", "Corrugated packaging, pallets and films",
                    "Sara Amrani", "contact@emballages-nord.ma", "+212 539-962214", "Quartier Industriel, Lot 7",
                    "Tetouan", "Morocco", "93000", List.of("Check", "30 days"), 30,
                    CurrencyCode.MAD, true, 10, 0.78, 0.09, 4, 10),
            new SupplierSpec("CHIMIPRO", "ChimiPro Distribution", "Industrial chemicals, paints and lubricants",
                    "Driss Lahlou", "ventes@chimipro.ma", "+212 523-301877", "Zone Industrielle Sud-Ouest, Lot 112",
                    "Mohammedia", "Morocco", "28810", List.of("Bank transfer", "45 days"), 45,
                    CurrencyCode.MAD, true, 8, 0.85, 0.06, 4, 11),
            new SupplierSpec("KRT", "Kenitra Roulements & Transmission", "Bearings, belts and power transmission",
                    "Younes Sqalli", "service@krt.ma", "+212 537-379055", "45 Avenue Mohammed V",
                    "Kenitra", "Morocco", "14000", List.of("Bank transfer", "60 days"), 60,
                    CurrencyCode.MAD, true, 8, 0.80, 0.05, 3, 9),
            new SupplierSpec("SAFEWORK", "SafeWork Maroc", "Personal protective equipment and welding consumables",
                    "Hind Zniber", "commandes@safework.ma", "+212 524-436178", "Quartier Industriel Sidi Ghanem, 221",
                    "Marrakech", "Morocco", "40110", List.of("Bank transfer", "30 days"), 30,
                    CurrencyCode.MAD, true, 8, 0.92, 0.03, 2, 7),
            new SupplierSpec("OUTILPRO", "Outillage Pro Agadir", "Hand tools, power tools and metrology",
                    "Brahim Ait Lahcen", "info@outillagepro.ma", "+212 528-227409", "Avenue Hassan II, Imm. 14",
                    "Agadir", "Morocco", "80000", List.of("Check", "Cash on delivery", "30 days"), 30,
                    CurrencyCode.MAD, true, 5, 0.86, 0.04, 3, 10),
            new SupplierSpec("RHEIN", "Rhein Industrietechnik GmbH", "Hydraulic and pneumatic components",
                    "Jonas Weber", "export@rhein-industrietechnik.de", "+49 203 5590 210", "Hafenstrasse 88",
                    "Duisburg", "Germany", "47119", List.of("Bank transfer", "30 days"), 30,
                    CurrencyCode.EUR, true, 4, 0.95, 0.02, 18, 28),
            new SupplierSpec("IBERICA", "Iberica Componentes S.L.", "Industrial automation components",
                    "Lucia Moreno", "pedidos@iberica-componentes.es", "+34 961 220 457", "Poligono Fuente del Jarro, C/ 4",
                    "Valencia", "Spain", "46988", List.of("Bank transfer", "45 days"), 45,
                    CurrencyCode.EUR, true, 4, 0.90, 0.04, 14, 24),
            new SupplierSpec("FESIND", "Fes Industrial Supplies", "General industrial supplies (contract ended)",
                    "Abdellah Benjelloun", "contact@fes-industrial.ma", "+212 535-654120", "Zone Industrielle Ain Chkef",
                    "Fes", "Morocco", "30050", List.of("Check", "30 days"), 30,
                    CurrencyCode.MAD, false, 0, 0.7, 0.1, 5, 10));

    static final List<MaterialSpec> MATERIALS = List.of(
            // Steel & Metals
            new MaterialSpec("Hot-rolled steel sheet S235 2mm", "Hot-rolled S235JR sheet, 2000x1000mm, pickled and oiled",
                    RAW_MATERIAL, KG, "METAL", "ATLAS", "14.50", 2500, 1000, 15000, 4000, 3200),
            new MaterialSpec("Galvanized steel coil DX51D 0.8mm", "Hot-dip galvanized coil Z275, width 1250mm",
                    RAW_MATERIAL, KG, "METAL", "ATLAS", "17.80", 3000, 1200, 18000, 5000, 3600).target(StockTarget.REORDER),
            new MaterialSpec("Steel round bar C45 40mm", "Cold-drawn C45 round bar, 6m lengths",
                    RAW_MATERIAL, M, "METAL", "ATLAS", "92.00", 120, 50, 800, 240, 180),
            new MaterialSpec("Stainless steel tube 304 25mm", "AISI 304 welded tube 25x1.5mm, 6m lengths",
                    RAW_MATERIAL, M, "METAL", "ATLAS", "115.00", 90, 40, 600, 180, 110),
            new MaterialSpec("Aluminium sheet 5754 3mm", "Aluminium 5754 H111 sheet, 2500x1250mm",
                    RAW_MATERIAL, KG, "METAL", "ATLAS", "48.00", 400, 150, 2500, 700, 520).target(StockTarget.CRITICAL),
            // Plastics & Polymers
            new MaterialSpec("HDPE granules injection grade", "High-density polyethylene, MFI 8, 25kg bags",
                    RAW_MATERIAL, KG, "PLASTIC", "POLY", "16.20", 2000, 800, 12000, 3500, 2900),
            new MaterialSpec("PP copolymer granules", "Polypropylene block copolymer, MFI 12, 25kg bags",
                    RAW_MATERIAL, KG, "PLASTIC", "POLY", "15.40", 1800, 700, 10000, 3000, 2400),
            new MaterialSpec("Black masterbatch", "Carbon black masterbatch 40%, 25kg bags",
                    RAW_MATERIAL, KG, "PLASTIC", "POLY", "38.00", 150, 60, 900, 300, 210).target(StockTarget.OUT),
            new MaterialSpec("Rigid PVC sheet 5mm", "Grey rigid PVC sheet, 3000x1500mm",
                    RAW_MATERIAL, SHEET, "PLASTIC", "POLY", "640.00", 20, 8, 120, 40, 26),
            // Electrical Components
            new MaterialSpec("Copper cable H07V-K 2.5mm2", "Flexible single-core copper cable, 100m reels",
                    COMPONENT, M, "ELEC", "SOFELEC", "6.80", 800, 300, 5000, 1500, 1100),
            new MaterialSpec("Circuit breaker 3P 32A", "Modular MCB 3-pole, curve C, 10kA",
                    ELECTRONIC, PCE, "ELEC", "SOFELEC", "285.00", 15, 6, 80, 25, 14),
            new MaterialSpec("LED panel 60x60 40W", "LED ceiling panel 4000K, 4000lm",
                    ELECTRONIC, PCE, "ELEC", "SOFELEC", "240.00", 20, 8, 120, 40, 18),
            new MaterialSpec("Contactor 3P 25A 230V AC", "Power contactor AC-3, 1NO+1NC auxiliary",
                    ELECTRONIC, PCE, "ELEC", "IBERICA", "42.00", 12, 5, 60, 20, 9),
            new MaterialSpec("Inductive proximity sensor M18 PNP", "Flush mounting, 8mm sensing range, M12 connector",
                    ELECTRONIC, PCE, "ELEC", "IBERICA", "28.50", 20, 8, 100, 40, 16).target(StockTarget.REORDER),
            // Packaging
            new MaterialSpec("Corrugated box 600x400x400", "Double-wall corrugated box, brown kraft",
                    PACKAGING, PCE, "PACK", "NORDPACK", "9.80", 1500, 600, 8000, 3000, 2600),
            new MaterialSpec("Wooden pallet EUR 1200x800", "EPAL-type heat-treated wooden pallet",
                    PACKAGING, PAL, "PACK", "NORDPACK", "95.00", 150, 60, 800, 300, 260),
            new MaterialSpec("Stretch film 23 microns 500mm", "Machine stretch film, 17kg rolls",
                    PACKAGING, ROLL, "PACK", "NORDPACK", "185.00", 30, 12, 150, 60, 48),
            new MaterialSpec("PP strapping 12mm", "Polypropylene strapping band, 2500m coils",
                    PACKAGING, ROLL, "PACK", "NORDPACK", "120.00", 25, 10, 120, 40, 30).target(StockTarget.CRITICAL),
            // Chemicals & Lubricants
            new MaterialSpec("Hydraulic oil ISO VG 46", "Anti-wear hydraulic oil, 208L drums",
                    CHEMICAL, L, "CHEM", "CHIMIPRO", "31.00", 600, 250, 3000, 1040, 650),
            new MaterialSpec("Epoxy primer grey", "Two-component epoxy primer, 20L kits",
                    CHEMICAL, L, "CHEM", "CHIMIPRO", "78.00", 200, 80, 1200, 400, 260),
            new MaterialSpec("Industrial degreaser", "Water-based alkaline degreaser, 20L cans",
                    CHEMICAL, L, "CHEM", "CHIMIPRO", "22.00", 300, 120, 1600, 600, 380),
            new MaterialSpec("Cutting fluid concentrate", "Semi-synthetic soluble cutting fluid, 20L cans",
                    CHEMICAL, L, "CHEM", "CHIMIPRO", "45.00", 160, 60, 800, 300, 170).target(StockTarget.REORDER),
            // Spare Parts
            new MaterialSpec("Ball bearing 6205-2RS", "Deep groove ball bearing, sealed both sides",
                    SPARE_PART, PCE, "SPARE", "KRT", "38.00", 40, 15, 200, 80, 45),
            new MaterialSpec("V-belt SPA 1250", "Narrow V-belt, wrapped, oil resistant",
                    SPARE_PART, PCE, "SPARE", "KRT", "64.00", 20, 8, 100, 40, 18).target(StockTarget.OUT),
            new MaterialSpec("Hydraulic filter element 10 microns", "Return-line filter element, glass fibre",
                    SPARE_PART, PCE, "SPARE", "RHEIN", "54.00", 12, 5, 60, 24, 10),
            new MaterialSpec("Pneumatic cylinder 50x200", "ISO 15552 double-acting cylinder, cushioned",
                    SPARE_PART, PCE, "SPARE", "RHEIN", "165.00", 6, 2, 30, 10, 4).target(StockTarget.CRITICAL),
            // Consumables & PPE
            new MaterialSpec("Welding wire ER70S-6 1.0mm", "Copper-coated MIG wire, 15kg spools",
                    CONSUMABLE, KG, "PPE", "SAFEWORK", "29.00", 300, 120, 1500, 600, 420),
            new MaterialSpec("Cutting disc 125x1.0mm", "Inox cutting disc for angle grinder",
                    CONSUMABLE, PCE, "PPE", "SAFEWORK", "7.50", 400, 150, 2500, 1000, 650),
            new MaterialSpec("Nitrile-coated safety gloves", "Cut level B gloves, EN388, sizes 8-10",
                    CONSUMABLE, PCE, "PPE", "SAFEWORK", "14.00", 300, 120, 1500, 600, 420).target(StockTarget.REORDER),
            new MaterialSpec("Safety helmet EN397", "ABS safety helmet with ratchet harness",
                    CONSUMABLE, PCE, "PPE", "SAFEWORK", "65.00", 30, 10, 150, 50, 14),
            // Tools & Instruments
            new MaterialSpec("Cordless drill 18V", "Brushless drill-driver with 2 batteries and charger",
                    TOOL, PCE, "TOOL", "OUTILPRO", "1450.00", 3, 1, 15, 4, 1),
            new MaterialSpec("Torque wrench 40-200Nm", "Click-type torque wrench, 1/2 inch drive",
                    TOOL, PCE, "TOOL", "OUTILPRO", "980.00", 2, 1, 10, 3, 1),
            new MaterialSpec("Digital caliper 150mm", "Stainless digital caliper, 0.01mm resolution",
                    TOOL, PCE, "TOOL", "OUTILPRO", "320.00", 4, 2, 20, 6, 2),
            // Discontinued items, never ordered
            new MaterialSpec("Mercury vapor lamp 250W", "Replaced by LED high-bay fixtures",
                    ELECTRONIC, PCE, "ELEC", "SOFELEC", "95.00", 0, 0, 0, 0, 0).status(MaterialStatus.OBSOLETE),
            new MaterialSpec("Solvent-based degreaser", "Withdrawn for health and safety reasons",
                    CHEMICAL, L, "CHEM", "FESIND", "35.00", 0, 0, 0, 0, 0).status(MaterialStatus.BLOCKED));

    /** Why a requester asks for goods, by category. */
    static final java.util.Map<String, List<String>> REQUISITION_PURPOSES = java.util.Map.of(
            "METAL", List.of("Monthly steel replenishment", "Raw material for work order", "Frame production run", "Stock rebuild after inventory"),
            "PLASTIC", List.of("Injection line resin supply", "Polymer stock for next production run", "Moulding campaign materials"),
            "ELEC", List.of("Electrical maintenance stock", "Control cabinet upgrade", "Workshop lighting renewal", "Line 3 automation retrofit"),
            "PACK", List.of("Packaging for customer shipments", "Shipping season packaging stock", "Pallet and film replenishment"),
            "CHEM", List.of("Lubricants for preventive maintenance", "Paint shop consumables", "Machining fluids replenishment"),
            "SPARE", List.of("Preventive maintenance spare parts", "Breakdown repair on press line", "Critical spare parts stock"),
            "PPE", List.of("PPE renewal for production staff", "Welding shop consumables", "New hires safety equipment"),
            "TOOL", List.of("Maintenance team tooling", "Quality lab instruments", "Replacement of worn-out tools"));

    /** Why goods of a category are rejected at receipt. */
    static final java.util.Map<String, List<String>> REJECTION_REASONS = java.util.Map.of(
            "METAL", List.of("Surface corrosion on several sheets", "Thickness out of tolerance", "Edges damaged during unloading"),
            "PLASTIC", List.of("Moisture content above specification", "Contaminated granules (foreign particles)", "Torn bags, material spilled"),
            "ELEC", List.of("Broken housings, damaged packaging", "Wrong reference delivered", "Failed functional test at incoming inspection"),
            "PACK", List.of("Crushed boxes", "Dimensions do not match the order", "Broken boards on pallets"),
            "CHEM", List.of("Leaking containers", "Batch close to expiry date", "Missing safety data sheet and labels"),
            "SPARE", List.of("Wrong dimensions", "Damaged in transit", "Counterfeit marking suspected"),
            "PPE", List.of("Wrong sizes delivered", "Missing CE marking", "Defective stitching"),
            "TOOL", List.of("Device does not power on", "Missing calibration certificate", "Damaged case and accessories"));
}
