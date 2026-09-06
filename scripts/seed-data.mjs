/**
 * Materia Test Data Seeder
 * 
 * Seeds realistic, production-like fake data into the Materia system:
 * 1. Categories (Roots -> Children)
 * 2. Suppliers
 * 3. Materials (Linked to Categories & Suppliers)
 *
 * Usage: node scripts/seed-data.mjs
 */

const BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api/v1/masterdata';
const CREATED_BY = 'TEST_DATA_SEEDER';

// ============================================================
// DATA SET DEFINITIONS
// ============================================================

// 1. ROOT CATEGORIES
const ROOT_CATEGORIES = [
  {
    name: 'Raw Materials',
    description: 'Primary unprocessed materials and industrial commodities used in manufacturing',
    shortDescription: 'Primary raw materials',
    categoryType: 'RMC',
    status: 'ACTIVE',
  },
  {
    name: 'Electronic Components',
    description: 'Active, passive, and electromechanical electronic parts for assemblies',
    shortDescription: 'Electronic components',
    categoryType: 'ELC',
    status: 'ACTIVE',
  },
  {
    name: 'Packaging & Logistics',
    description: 'Industrial packaging, shipping containers, protective wraps, and labeling supplies',
    shortDescription: 'Packaging supplies',
    categoryType: 'PKG',
    status: 'ACTIVE',
  },
  {
    name: 'Tools & Workshop Equipment',
    description: 'Manual and power tooling, precision measurement devices, and workshop fixtures',
    shortDescription: 'Tools and equipment',
    categoryType: 'TOL',
    status: 'ACTIVE',
  },
  {
    name: 'Industrial Chemicals & Fluids',
    description: 'Adhesives, sealants, lubricants, solvents, and surface treatment chemicals',
    shortDescription: 'Chemicals and lubricants',
    categoryType: 'CHM',
    status: 'ACTIVE',
  },
];

// 2. CHILD CATEGORIES (references parent by name)
const CHILD_CATEGORIES = [
  // Under Raw Materials
  {
    parentName: 'Raw Materials',
    name: 'Steel & Structural Metals',
    description: 'Carbon steel sheets, stainless rods, structural profiles, and metal bars',
    shortDescription: 'Steel and metals',
    categoryType: 'RMC',
    status: 'ACTIVE',
  },
  {
    parentName: 'Raw Materials',
    name: 'Copper & Non-Ferrous Alloys',
    description: 'Pure copper conductors, brass billets, aluminum profiles, and bronze bushings',
    shortDescription: 'Copper and brass',
    categoryType: 'RMC',
    status: 'ACTIVE',
  },
  {
    parentName: 'Raw Materials',
    name: 'Plastics & Technical Polymers',
    description: 'Polymer granules, extruded plastic sheets, POM, PTFE, and nylon stock',
    shortDescription: 'Technical polymers',
    categoryType: 'RMC',
    status: 'ACTIVE',
  },
  {
    parentName: 'Raw Materials',
    name: 'Wood & Composite Panels',
    description: 'Hardwood lumber, structural plywood, MDF sheets, and composite laminates',
    shortDescription: 'Wood and panels',
    categoryType: 'RMC',
    status: 'ACTIVE',
  },

  // Under Electronic Components
  {
    parentName: 'Electronic Components',
    name: 'Semiconductors & ICs',
    description: 'Microcontrollers, power MOSFETs, operational amplifiers, and voltage regulators',
    shortDescription: 'Microchips and ICs',
    categoryType: 'ELC',
    status: 'ACTIVE',
  },
  {
    parentName: 'Electronic Components',
    name: 'Passive Components',
    description: 'SMD ceramic capacitors, precision metal film resistors, and power inductors',
    shortDescription: 'Resistors and caps',
    categoryType: 'CMP',
    status: 'ACTIVE',
  },
  {
    parentName: 'Electronic Components',
    name: 'Connectors, Cables & Wire',
    description: 'Terminal blocks, industrial wire harnesses, USB-C jacks, and Ethernet patch cables',
    shortDescription: 'Cables and connectors',
    categoryType: 'CMP',
    status: 'ACTIVE',
  },
  {
    parentName: 'Electronic Components',
    name: 'Sensors & Transducers',
    description: 'Industrial temperature probes, optical sensors, pressure transducers, and encoders',
    shortDescription: 'Industrial sensors',
    categoryType: 'ELC',
    status: 'ACTIVE',
  },

  // Under Packaging & Logistics
  {
    parentName: 'Packaging & Logistics',
    name: 'Cardboard Boxes & Cartons',
    description: 'Double-wall corrugated shipping cartons, die-cut mailers, and pallet boxes',
    shortDescription: 'Corrugated cartons',
    categoryType: 'PKG',
    status: 'ACTIVE',
  },
  {
    parentName: 'Packaging & Logistics',
    name: 'Protective & Stretch Film',
    description: 'High-tensile stretch wrap, air bubble film, foam inserts, and strapping tape',
    shortDescription: 'Films and bubble wrap',
    categoryType: 'PKG',
    status: 'ACTIVE',
  },
  {
    parentName: 'Packaging & Logistics',
    name: 'Labels, RFID & Identification',
    description: 'Direct thermal barcode labels, tamper-evident seals, and HF RFID tags',
    shortDescription: 'Barcode labels',
    categoryType: 'PKG',
    status: 'ACTIVE',
  },

  // Under Tools & Workshop Equipment
  {
    parentName: 'Tools & Workshop Equipment',
    name: 'Precision Hand Tools',
    description: 'Calibrated torque wrenches, screwdrivers, crimpers, and ESD safe hand tools',
    shortDescription: 'Hand tools',
    categoryType: 'TOL',
    status: 'ACTIVE',
  },
  {
    parentName: 'Tools & Workshop Equipment',
    name: 'Cutting & Machining Tools',
    description: 'Carbide end mills, high-speed drill bits, abrasive cutting discs, and deburring blades',
    shortDescription: 'Milling and drills',
    categoryType: 'TOL',
    status: 'ACTIVE',
  },
  {
    parentName: 'Tools & Workshop Equipment',
    name: 'Measuring & Inspection Instruments',
    description: 'Digital vernier calipers, micrometers, dial test indicators, and infrared thermometers',
    shortDescription: 'Calipers and meters',
    categoryType: 'TOL',
    status: 'ACTIVE',
  },

  // Under Industrial Chemicals & Fluids
  {
    parentName: 'Industrial Chemicals & Fluids',
    name: 'Industrial Adhesives & Sealants',
    description: 'Two-component structural epoxy, anaerobic threadlockers, and silicone sealants',
    shortDescription: 'Epoxy and sealants',
    categoryType: 'CHM',
    status: 'ACTIVE',
  },
  {
    parentName: 'Industrial Chemicals & Fluids',
    name: 'Cleaning & Degreasing Solvents',
    description: 'Electronics-grade isopropyl alcohol, industrial degreasers, and ultrasonic wash solutions',
    shortDescription: 'Cleaners and solvents',
    categoryType: 'CHM',
    status: 'ACTIVE',
  },
  {
    parentName: 'Industrial Chemicals & Fluids',
    name: 'Lubricants & Industrial Oils',
    description: 'Synthetic gear lubricants, pneumatic oil, silicone greases, and anti-seize paste',
    shortDescription: 'Oils and greases',
    categoryType: 'CHM',
    status: 'ACTIVE',
  },
];

// 3. SUPPLIERS
const SUPPLIERS = [
  {
    name: 'Atlas Steel Maroc SARL',
    description: 'Primary manufacturer and distributor of structural steel, rebar, and carbon sheets in North Africa',
    contactPerson: 'Youssef Benali',
    contactEmail: 'y.benali@atlassteel.ma',
    contactPhone: '+212 522-348910',
    address: 'Zone Industrielle Sidi Bernoussi, Boulevard Chefchaouni Lot 45',
    city: 'Casablanca',
    country: 'Morocco',
    postalCode: '20590',
    paymentTerms: ['NET_30', 'BANK_TRANSFER'],
    currencyCode: 'MAD',
  },
  {
    name: 'Maghreb Electronics Distribution',
    description: 'Authorized national distributor of microcontrollers, passive passives, and semiconductor assemblies',
    contactPerson: 'Fatima Zahra Mansouri',
    contactEmail: 'f.mansouri@maghrebelectro.ma',
    contactPhone: '+212 537-772244',
    address: 'Technopolis Rabatshore, Bâtiment B3',
    city: 'Rabat',
    country: 'Morocco',
    postalCode: '11100',
    paymentTerms: ['NET_60', 'CHECK'],
    currencyCode: 'MAD',
  },
  {
    name: 'EuroPack Solutions SAS',
    description: 'European leader in high-performance corrugated packaging, honeycomb boards, and eco-mailers',
    contactPerson: 'Antoine Dupont',
    contactEmail: 'a.dupont@europack-solutions.fr',
    contactPhone: '+33 4 72 88 19 20',
    address: '14 Rue de la Métallurgie, Zone Industrielle Nord',
    city: 'Lyon',
    country: 'France',
    postalCode: '69007',
    paymentTerms: ['NET_30', 'BANK_TRANSFER', 'CREDIT_CARD'],
    currencyCode: 'EUR',
  },
  {
    name: 'Rhine Specialty Chemicals GmbH',
    description: 'High-purity industrial solvents, technical lubricants, UV curable adhesives, and bonding compounds',
    contactPerson: 'Klaus Schmidt',
    contactEmail: 'k.schmidt@rhinechem.de',
    contactPhone: '+49 69 9005 440',
    address: 'Industriepark Höchst, Gebäude C480',
    city: 'Frankfurt',
    country: 'Germany',
    postalCode: '65926',
    paymentTerms: ['NET_45', 'WIRE_TRANSFER'],
    currencyCode: 'EUR',
  },
  {
    name: 'Shenzhen Precision Components Ltd',
    description: 'Global OEM component exporter specializing in high-speed interconnects, terminal blocks, and custom cable looms',
    contactPerson: 'Wei Chen',
    contactEmail: 'w.chen@sz-precision.cn',
    contactPhone: '+86 755 8320 9811',
    address: 'Tower B, High-Tech Industrial Park, Nanshan District',
    city: 'Shenzhen',
    country: 'China',
    postalCode: '518057',
    paymentTerms: ['LC', 'ADVANCE_PAYMENT'],
    currencyCode: 'USD',
  },
  {
    name: 'Apex Industrial Tools Inc',
    description: 'Industrial-grade precision torque equipment, pneumatic tools, and calibrated workshop instruments',
    contactPerson: 'David Miller',
    contactEmail: 'dmiller@apextoolcorp.com',
    contactPhone: '+1 312-555-0199',
    address: '450 North Michigan Avenue, Suite 1200',
    city: 'Chicago',
    country: 'USA',
    postalCode: '60611',
    paymentTerms: ['NET_30', 'CREDIT_CARD'],
    currencyCode: 'USD',
  },
  {
    name: 'MedPharma Packaging Maroc',
    description: 'Specialized blister packs, sterile barrier pouches, and pharmaceutical-grade vials',
    contactPerson: 'Samir El Amrani',
    contactEmail: 'samir.elamrani@medpharma.ma',
    contactPhone: '+212 539-392100',
    address: 'Tanger Free Zone, Îlot 14',
    city: 'Tangier',
    country: 'Morocco',
    postalCode: '90000',
    paymentTerms: ['NET_30'],
    currencyCode: 'MAD',
  },
  {
    name: 'Nordic Fasteners & Hardware AB',
    description: 'Marine-grade stainless steel fasteners, metric machine screws, retaining rings, and dowels',
    contactPerson: 'Lars Lindqvist',
    contactEmail: 'lars.lindqvist@nordicfasteners.se',
    contactPhone: '+46 31 704 2200',
    address: 'Hamngatan 8, Port of Gothenburg Industrial Area',
    city: 'Gothenburg',
    country: 'Sweden',
    postalCode: '41106',
    paymentTerms: ['NET_60', 'BANK_TRANSFER'],
    currencyCode: 'EUR',
  },
  {
    name: 'Iberian Polymers & Resins SL',
    description: 'Engineering thermoplastic raw materials, high-density polyethylene pellets, and masterbatch dyes',
    contactPerson: 'Elena Garcia',
    contactEmail: 'egarcia@iberianpolymers.es',
    contactPhone: '+34 93 481 7200',
    address: 'Polígono Industrial Zona Franca, Calle D 18',
    city: 'Barcelona',
    country: 'Spain',
    postalCode: '08040',
    paymentTerms: ['NET_30', 'CASH_ON_DELIVERY'],
    currencyCode: 'EUR',
  },
  {
    name: 'Atlas Packaging & Containers',
    description: 'Heavy carton manufacturing, fruit export trays, corrugated dividers, and adhesive sealing tape',
    contactPerson: 'Omar Tazi',
    contactEmail: 'omar.tazi@atlaspack.ma',
    contactPhone: '+212 524-338811',
    address: 'Zone Industrielle Sidi Ghanem, Rue Principale N° 218',
    city: 'Marrakech',
    country: 'Morocco',
    postalCode: '40000',
    paymentTerms: ['CASH', 'BANK_TRANSFER'],
    currencyCode: 'MAD',
  },
  {
    name: 'Bengal Industrial Textiles Ltd',
    description: 'Durable cotton canvas covers, polyester filtration cloths, and woven geotextile fabrics',
    contactPerson: 'Rafiqul Islam',
    contactEmail: 'rafiq@bengaltextiles.com.bd',
    contactPhone: '+880 2 988 5601',
    address: 'Plot 72, Tejgaon Industrial Area',
    city: 'Dhaka',
    country: 'Bangladesh',
    postalCode: '1208',
    paymentTerms: ['LC', 'NET_90'],
    currencyCode: 'USD',
  },
  {
    name: 'Sahara Mining & Minerals SARL',
    description: 'Industrial silica sand, gypsum, calcium carbonate powders, and mineral fillers',
    contactPerson: 'Brahim Ait Ouhssain',
    contactEmail: 'b.aitouhssain@saharamining.ma',
    contactPhone: '+212 528-842911',
    address: 'Route Principale d\'Anza, Km 8',
    city: 'Agadir',
    country: 'Morocco',
    postalCode: '80000',
    paymentTerms: ['NET_15'],
    currencyCode: 'MAD',
  },
  {
    name: 'Bavaria Metrology & Sensors AG',
    description: 'Precision coordinate measurement tools, optical encoders, laser micrometers, and calibration rigs',
    contactPerson: 'Hans Weber',
    contactEmail: 'h.weber@bavaria-sensors.de',
    contactPhone: '+49 89 4522 180',
    address: 'Leopoldstraße 140, Werk 2',
    city: 'Munich',
    country: 'Germany',
    postalCode: '80804',
    paymentTerms: ['NET_30', 'WIRE_TRANSFER'],
    currencyCode: 'EUR',
  },
  {
    name: 'TechnoPlast Maroc Industrie',
    description: 'Custom injection molding, technical POM gears, ABS enclosures, and automotive nylon manifolds',
    contactPerson: 'Nadia Chraibi',
    contactEmail: 'n.chraibi@technoplast.ma',
    contactPhone: '+212 535-654020',
    address: 'Zone Industrielle Bensouda, Lot 112',
    city: 'Fez',
    country: 'Morocco',
    postalCode: '30000',
    paymentTerms: ['NET_45', 'CHECK'],
    currencyCode: 'MAD',
  },
  {
    name: 'Pacific Advanced Materials Corp',
    description: 'Ultra-pure silicon ingots, ceramic substrates, optical glass fibers, and thermal interface sheets',
    contactPerson: 'Kenji Sato',
    contactEmail: 'k-sato@pacific-materials.co.jp',
    contactPhone: '+81 3 3287 4100',
    address: 'Chiyoda-ku, Marunouchi 2-4-1, Building 8F',
    city: 'Tokyo',
    country: 'Japan',
    postalCode: '100-6308',
    paymentTerms: ['NET_60', 'WIRE_TRANSFER'],
    currencyCode: 'USD',
  },
];

// 4. MATERIALS (references Category & Supplier by name)
// Rules enforced: safetyStock <= reorderPoint <= minimumStock <= maximumStock
const MATERIALS = [
  // --- Category: Steel & Structural Metals ---
  {
    categoryName: 'Steel & Structural Metals',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Cold-Rolled Carbon Steel Sheet 2mm',
    description: 'Cold-rolled commercial carbon steel plate, 2.0mm thickness, standard sheet size 1250x2500mm, ASTM A1008 compliant',
    shortDescription: '2mm carbon steel sheet',
    searchKeywords: 'steel sheet plate carbon cold-rolled 2mm ASTM',
    alternativeName: 'CR Sheet 2.0mm Grade DC01',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'KG',
    currentStock: 4500,
    minimumStock: 1500,
    maximumStock: 12000,
    reorderPoint: 1000,
    safetyStock: 500,
    economicOrderQuantity: 3000,
    standardPrice: 12.80,
    standardPriceCurrency: 'MAD',
    costPrice: 10.50,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Steel & Structural Metals',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Stainless Steel Rod 316L Ø12mm',
    description: 'Austenitic marine-grade stainless steel round bar, cold-drawn polished finish, 3-meter length',
    shortDescription: '316L SS rod Ø12mm',
    searchKeywords: 'stainless steel rod round bar 316L marine grade',
    alternativeName: 'Inox Bar 12mm 316L',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'M',
    currentStock: 180, // Below minimum! (180 < 400)
    minimumStock: 400,
    maximumStock: 1500,
    reorderPoint: 250,
    safetyStock: 100,
    economicOrderQuantity: 500,
    standardPrice: 95.00,
    standardPriceCurrency: 'MAD',
    costPrice: 78.50,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Steel & Structural Metals',
    supplierName: 'Nordic Fasteners & Hardware AB',
    name: 'Galvanized Hex Head Bolt M8x40 Grade 8.8',
    description: 'Hot-dip galvanized high-tensile steel hexagon screw, fully threaded ISO 4017 Grade 8.8',
    shortDescription: 'M8x40 Hex Bolt 8.8',
    searchKeywords: 'hex bolt screw fastener M8 40mm galvanized 8.8',
    alternativeName: 'Vis Tête Hexagonale M8x40',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'BOX',
    currentStock: 240,
    minimumStock: 80,
    maximumStock: 800,
    reorderPoint: 60,
    safetyStock: 30,
    economicOrderQuantity: 200,
    standardPrice: 14.50,
    standardPriceCurrency: 'EUR',
    costPrice: 10.20,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Steel & Structural Metals',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Square Structural Steel Tube 50x50x3mm',
    description: 'Hollow structural section carbon steel tube, welded seam, black finish, 6m length per unit',
    shortDescription: 'Square tube 50x50x3',
    searchKeywords: 'steel tube profile hollow section square 50x50',
    alternativeName: 'Tube Carré Acier 50x50',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'M',
    currentStock: 0, // OUT OF STOCK!
    minimumStock: 250,
    maximumStock: 1200,
    reorderPoint: 150,
    safetyStock: 60,
    economicOrderQuantity: 400,
    standardPrice: 62.00,
    standardPriceCurrency: 'MAD',
    costPrice: 51.00,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Copper & Non-Ferrous Alloys ---
  {
    categoryName: 'Copper & Non-Ferrous Alloys',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Electrolytic Copper Busbar 30x5mm',
    description: 'High conductivity copper flat bar (Cu-ETP 99.9%), rounded edges for electrical switchboards',
    shortDescription: 'Copper busbar 30x5',
    searchKeywords: 'copper busbar flat bar electrical conductor ETP',
    alternativeName: 'Barre Cuivre 30x5mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'KG',
    currentStock: 680,
    minimumStock: 300,
    maximumStock: 2500,
    reorderPoint: 200,
    safetyStock: 100,
    economicOrderQuantity: 600,
    standardPrice: 110.00,
    standardPriceCurrency: 'MAD',
    costPrice: 92.00,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Copper & Non-Ferrous Alloys',
    supplierName: 'Nordic Fasteners & Hardware AB',
    name: 'Free-Machining Brass Hex Bar 19mm',
    description: 'Leaded brass hexagonal stock alloy CuZn39Pb3 (CW614N) for CNC screw machining',
    shortDescription: 'Brass hex bar 19mm',
    searchKeywords: 'brass hex rod bar CW614N machining',
    alternativeName: 'Barre Laiton Hexagonale 19mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'KG',
    currentStock: 420,
    minimumStock: 250,
    maximumStock: 1800,
    reorderPoint: 180,
    safetyStock: 80,
    economicOrderQuantity: 500,
    standardPrice: 8.90,
    standardPriceCurrency: 'EUR',
    costPrice: 6.80,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Copper & Non-Ferrous Alloys',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Insulated Copper Hookup Wire 1.5mm² Red',
    description: 'Single core PVC insulated copper flexible electrical installation wire H07V-K, 100m spool',
    shortDescription: 'Wire 1.5mm² red 100m',
    searchKeywords: 'copper wire electrical cable 1.5mm red spool',
    alternativeName: 'Fil H07V-K 1.5 Rouge',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'ROLL',
    currentStock: 35, // Below minimum! (35 < 60)
    minimumStock: 60,
    maximumStock: 200,
    reorderPoint: 45,
    safetyStock: 20,
    economicOrderQuantity: 50,
    standardPrice: 280.00,
    standardPriceCurrency: 'MAD',
    costPrice: 225.00,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Plastics & Technical Polymers ---
  {
    categoryName: 'Plastics & Technical Polymers',
    supplierName: 'Iberian Polymers & Resins SL',
    name: 'Virgin ABS Pellets Natural Grade HF380',
    description: 'High impact Acrylonitrile Butadiene Styrene injection molding resin granules, 25kg bag',
    shortDescription: 'ABS pellets natural 25kg',
    searchKeywords: 'ABS plastic resin pellets injection moulding granules',
    alternativeName: 'Granulés ABS Naturel',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'KG',
    currentStock: 3200,
    minimumStock: 2000,
    maximumStock: 15000,
    reorderPoint: 1500,
    safetyStock: 600,
    economicOrderQuantity: 5000,
    standardPrice: 2.65,
    standardPriceCurrency: 'EUR',
    costPrice: 2.10,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Plastics & Technical Polymers',
    supplierName: 'TechnoPlast Maroc Industrie',
    name: 'Polyacetal POM-C White Rod Ø40mm',
    description: 'Engineering plastic polyoxymethylene copolymer rod, high dimensional stability, 1-meter stick',
    shortDescription: 'POM-C rod Ø40mm',
    searchKeywords: 'POM delrin rod polyacetal plastic engineering rod 40mm',
    alternativeName: 'Rond Delrin Blanc 40mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 85,
    minimumStock: 50,
    maximumStock: 250,
    reorderPoint: 35,
    safetyStock: 15,
    economicOrderQuantity: 50,
    standardPrice: 195.00,
    standardPriceCurrency: 'MAD',
    costPrice: 155.00,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Plastics & Technical Polymers',
    supplierName: 'Iberian Polymers & Resins SL',
    name: 'Clear Polycarbonate Sheet 4mm UV-Protected',
    description: 'Extruded high-clarity shatterproof polycarbonate glazing sheet 2050x1250mm, 2-side UV layer',
    shortDescription: 'PC sheet 4mm UV 2x1.25m',
    searchKeywords: 'polycarbonate sheet clear plastic acrylic plexiglass 4mm',
    alternativeName: 'Plaque Polycarbonate 4mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'SHEET',
    currentStock: 42,
    minimumStock: 25,
    maximumStock: 120,
    reorderPoint: 20,
    safetyStock: 10,
    economicOrderQuantity: 30,
    standardPrice: 68.50,
    standardPriceCurrency: 'EUR',
    costPrice: 52.00,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Wood & Composite Panels ---
  {
    categoryName: 'Wood & Composite Panels',
    supplierName: 'Atlas Steel Maroc SARL',
    name: 'Birch Plywood Board 18mm BB/BB Grade',
    description: 'Multi-ply cross-bonded Baltic birch plywood sheet 2500x1250mm, interior furniture grade',
    shortDescription: 'Birch plywood 18mm',
    searchKeywords: 'plywood birch timber wood panel 18mm sheet',
    alternativeName: 'Contreplaqué Bouleau 18mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'SHEET',
    currentStock: 120,
    minimumStock: 60,
    maximumStock: 300,
    reorderPoint: 45,
    safetyStock: 20,
    economicOrderQuantity: 100,
    standardPrice: 540.00,
    standardPriceCurrency: 'MAD',
    costPrice: 420.00,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Wood & Composite Panels',
    supplierName: 'Atlas Packaging & Containers',
    name: 'Industrial Heat-Treated Pallet Wood Plank 1200x100x20mm',
    description: 'ISPM-15 kiln-dried pine wood slat for shipping crate and pallet assembly',
    shortDescription: 'Pallet slat 1.2m',
    searchKeywords: 'pallet wood pine slat lumber crate timber ISPM-15',
    alternativeName: 'Planche Palette Pin 1200mm',
    materialType: 'RMT',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 1400,
    minimumStock: 800,
    maximumStock: 5000,
    reorderPoint: 550,
    safetyStock: 250,
    economicOrderQuantity: 1500,
    standardPrice: 14.00,
    standardPriceCurrency: 'MAD',
    costPrice: 10.50,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Semiconductors & ICs ---
  {
    categoryName: 'Semiconductors & ICs',
    supplierName: 'Maghreb Electronics Distribution',
    name: 'STM32F401RET6 Microcontroller ARM Cortex-M4',
    description: '32-bit RISC MCU with 512KB Flash, 96KB RAM, 84MHz, LQFP-64 package',
    shortDescription: 'STM32 MCU LQFP64',
    searchKeywords: 'microcontroller STM32 ARM Cortex IC semiconductor 32bit',
    alternativeName: 'Microcontrôleur STM32F401',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 850,
    minimumStock: 400,
    maximumStock: 3000,
    reorderPoint: 300,
    safetyStock: 120,
    economicOrderQuantity: 500,
    standardPrice: 42.00,
    standardPriceCurrency: 'MAD',
    costPrice: 31.50,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Semiconductors & ICs',
    supplierName: 'Shenzhen Precision Components Ltd',
    name: 'N-Channel Power MOSFET 60V 50A TO-220',
    description: 'Ultra-low RDS(on) switching power MOSFET IRLZ44N, logic level gate drive, TO-220AB',
    shortDescription: 'MOSFET 60V 50A TO220',
    searchKeywords: 'MOSFET transistor semiconductor switch power TO220 IRLZ44N',
    alternativeName: 'Transistor MOSFET 60V',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 3200,
    minimumStock: 1000,
    maximumStock: 10000,
    reorderPoint: 750,
    safetyStock: 300,
    economicOrderQuantity: 2000,
    standardPrice: 0.65,
    standardPriceCurrency: 'USD',
    costPrice: 0.38,
    costPriceCurrency: 'USD',
  },
  {
    categoryName: 'Semiconductors & ICs',
    supplierName: 'Maghreb Electronics Distribution',
    name: 'Linear Voltage Regulator L7805CV 5V 1.5A',
    description: 'Positive fixed 3-terminal voltage stabilizer IC, internal thermal overload protection, TO-220',
    shortDescription: '7805 Regulator 5V 1.5A',
    searchKeywords: 'voltage regulator 7805 5V power IC linear TO220',
    alternativeName: 'Régulateur 7805 5V',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 0, // OUT OF STOCK
    minimumStock: 500,
    maximumStock: 2500,
    reorderPoint: 350,
    safetyStock: 150,
    economicOrderQuantity: 1000,
    standardPrice: 4.80,
    standardPriceCurrency: 'MAD',
    costPrice: 3.20,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Semiconductors & ICs',
    supplierName: 'Pacific Advanced Materials Corp',
    name: 'Dual Operational Amplifier LM358DR SOIC-8',
    description: 'Low power dual op-amp wide supply range 3V to 32V, standard SMD tape & reel packaging',
    shortDescription: 'LM358 Dual Op-Amp SO8',
    searchKeywords: 'opamp operational amplifier LM358 SOIC8 analog IC',
    alternativeName: 'Amplificateur Op LM358',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 5400,
    minimumStock: 2000,
    maximumStock: 15000,
    reorderPoint: 1500,
    safetyStock: 600,
    economicOrderQuantity: 3000,
    standardPrice: 0.22,
    standardPriceCurrency: 'USD',
    costPrice: 0.12,
    costPriceCurrency: 'USD',
  },

  // --- Category: Passive Components ---
  {
    categoryName: 'Passive Components',
    supplierName: 'Shenzhen Precision Components Ltd',
    name: 'SMD Ceramic Capacitor 100nF 50V X7R 0805',
    description: 'Surface mount multilayer ceramic chip capacitor (MLCC), ±10% tolerance, reel of 4000 pcs',
    shortDescription: 'Capacitor 100nF 0805',
    searchKeywords: 'capacitor MLCC SMD 100nF 0.1uF 0805 passive electronic',
    alternativeName: 'Condensateur Céramique 100nF',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'REEL',
    currentStock: 48,
    minimumStock: 20,
    maximumStock: 100,
    reorderPoint: 15,
    safetyStock: 8,
    economicOrderQuantity: 25,
    standardPrice: 12.00,
    standardPriceCurrency: 'USD',
    costPrice: 7.50,
    costPriceCurrency: 'USD',
  },
  {
    categoryName: 'Passive Components',
    supplierName: 'Maghreb Electronics Distribution',
    name: 'Metal Film Resistor 10kΩ 1/4W 1% Through-Hole',
    description: 'Axial leaded precision fixed resistor, low noise, high stability, pack of 1000 pcs',
    shortDescription: 'Resistor 10k 1/4W pack',
    searchKeywords: 'resistor 10k ohm metal film 1/4W through hole axial pack',
    alternativeName: 'Résistance 10k 0.25W',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'PACK',
    currentStock: 95,
    minimumStock: 45,
    maximumStock: 200,
    reorderPoint: 35,
    safetyStock: 15,
    economicOrderQuantity: 50,
    standardPrice: 65.00,
    standardPriceCurrency: 'MAD',
    costPrice: 42.00,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Passive Components',
    supplierName: 'Shenzhen Precision Components Ltd',
    name: 'Shielded Power Inductor 22µH 3.5A SMD',
    description: 'Ferrite core shielded power choke 12x12x7mm for DC-DC buck converters',
    shortDescription: 'Inductor 22uH 3.5A SMD',
    searchKeywords: 'inductor choke coil 22uH power SMD ferrite',
    alternativeName: 'Inductance Blindée 22uH',
    materialType: 'CMP',
    status: 'DRAFT',
    unitOfMeasure: 'PCE',
    currentStock: 250,
    minimumStock: 300, // Below minimum! (250 < 300)
    maximumStock: 1500,
    reorderPoint: 200,
    safetyStock: 80,
    economicOrderQuantity: 500,
    standardPrice: 0.85,
    standardPriceCurrency: 'USD',
    costPrice: 0.52,
    costPriceCurrency: 'USD',
  },

  // --- Category: Connectors, Cables & Wire ---
  {
    categoryName: 'Connectors, Cables & Wire',
    supplierName: 'Shenzhen Precision Components Ltd',
    name: 'USB Type-C Female Receptacle 16-Pin SMD',
    description: 'Waterproof mid-mount USB-C socket, nickel shell, gold-plated contacts, 5A fast charge support',
    shortDescription: 'USB-C receptacle 16p',
    searchKeywords: 'USB-C connector socket receptacle female SMD type-c',
    alternativeName: 'Connecteur USB-C Femelle',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 1800,
    minimumStock: 700,
    maximumStock: 5000,
    reorderPoint: 500,
    safetyStock: 200,
    economicOrderQuantity: 1000,
    standardPrice: 0.45,
    standardPriceCurrency: 'USD',
    costPrice: 0.28,
    costPriceCurrency: 'USD',
  },
  {
    categoryName: 'Connectors, Cables & Wire',
    supplierName: 'Maghreb Electronics Distribution',
    name: 'Cat6 UTP Industrial Patch Cable 3m Grey',
    description: 'Gigabit Ethernet patch cord RJ45 booted snagless, 24 AWG stranded copper conductors',
    shortDescription: 'Cat6 cable 3m RJ45',
    searchKeywords: 'ethernet cable RJ45 Cat6 network patch cord 3m',
    alternativeName: 'Câble Réseau Cat6 3m',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 110, // Below minimum! (110 < 180)
    minimumStock: 180,
    maximumStock: 600,
    reorderPoint: 140,
    safetyStock: 60,
    economicOrderQuantity: 200,
    standardPrice: 28.00,
    standardPriceCurrency: 'MAD',
    costPrice: 19.50,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Connectors, Cables & Wire',
    supplierName: 'Maghreb Electronics Distribution',
    name: 'Pluggable Terminal Block 5.08mm 4-Pin Green',
    description: 'Screw terminal plug-in connector header & socket pair, 300V 15A rating',
    shortDescription: 'Terminal block 4p 5.08',
    searchKeywords: 'terminal block connector phoenix screw pluggable 4 pin 5.08mm',
    alternativeName: 'Bornier Débrochable 4P',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'SET',
    currentStock: 650,
    minimumStock: 300,
    maximumStock: 2000,
    reorderPoint: 220,
    safetyStock: 90,
    economicOrderQuantity: 500,
    standardPrice: 5.50,
    standardPriceCurrency: 'MAD',
    costPrice: 3.80,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Sensors & Transducers ---
  {
    categoryName: 'Sensors & Transducers',
    supplierName: 'Bavaria Metrology & Sensors AG',
    name: 'PT100 RTD Temperature Probe Class A M6',
    description: 'Stainless steel sheathed platinum resistance thermometer, 2-meter Teflon cable, -50°C to +250°C',
    shortDescription: 'PT100 probe M6 2m',
    searchKeywords: 'PT100 sensor temperature RTD platinum probe industrial',
    alternativeName: 'Sonde Température PT100 M6',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 62,
    minimumStock: 35,
    maximumStock: 150,
    reorderPoint: 25,
    safetyStock: 10,
    economicOrderQuantity: 50,
    standardPrice: 38.00,
    standardPriceCurrency: 'EUR',
    costPrice: 27.50,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Sensors & Transducers',
    supplierName: 'Bavaria Metrology & Sensors AG',
    name: 'Inductive Proximity Sensor M12 NPN NO 4mm',
    description: 'Nickel-plated brass cylindrical proximity switch, flush mount, IP67 waterproof, LED indicator',
    shortDescription: 'Inductive M12 NPN 4mm',
    searchKeywords: 'proximity sensor inductive switch M12 NPN automation',
    alternativeName: 'Détecteur Inductif M12',
    materialType: 'ELC',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 95,
    minimumStock: 45,
    maximumStock: 200,
    reorderPoint: 35,
    safetyStock: 15,
    economicOrderQuantity: 50,
    standardPrice: 24.50,
    standardPriceCurrency: 'EUR',
    costPrice: 17.00,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Cardboard Boxes & Cartons ---
  {
    categoryName: 'Cardboard Boxes & Cartons',
    supplierName: 'EuroPack Solutions SAS',
    name: 'Double Wall Corrugated Box 400x300x250mm',
    description: 'Heavy duty BC-flute kraft brown packing box, max payload 30kg, pallet quantity 250 pcs',
    shortDescription: 'Box 40x30x25cm BC-flute',
    searchKeywords: 'cardboard box carton shipping packaging corrugated double wall',
    alternativeName: 'Carton Double Cannelure 40x30x25',
    materialType: 'PKG',
    status: 'ACTIVE',
    unitOfMeasure: 'PAL',
    currentStock: 18,
    minimumStock: 10,
    maximumStock: 50,
    reorderPoint: 8,
    safetyStock: 3,
    economicOrderQuantity: 15,
    standardPrice: 340.00,
    standardPriceCurrency: 'EUR',
    costPrice: 265.00,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Cardboard Boxes & Cartons',
    supplierName: 'Atlas Packaging & Containers',
    name: 'Self-Sealing Die-Cut Mailer Box 250x150x80mm',
    description: 'White micro-flute e-commerce post box with tear strip and peel & seal adhesive closure',
    shortDescription: 'Mailer box 25x15x8cm',
    searchKeywords: 'mailer box postal shipping ecommerce self adhesive white carton',
    alternativeName: 'Boîte Postale Autoadhésive 25x15x8',
    materialType: 'PKG',
    status: 'ACTIVE',
    unitOfMeasure: 'BOX',
    currentStock: 1200,
    minimumStock: 500,
    maximumStock: 4000,
    reorderPoint: 380,
    safetyStock: 150,
    economicOrderQuantity: 1000,
    standardPrice: 3.80,
    standardPriceCurrency: 'MAD',
    costPrice: 2.70,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Protective & Stretch Film ---
  {
    categoryName: 'Protective & Stretch Film',
    supplierName: 'EuroPack Solutions SAS',
    name: 'Manual Pallet Stretch Film 500mm x 300m 23µm',
    description: 'Cast coextruded transparent stretch wrap roll, puncture resistant, silent unwind, box of 6 rolls',
    shortDescription: 'Stretch wrap 500mm 23u',
    searchKeywords: 'stretch film wrap pallet plastic packaging cling film roll',
    alternativeName: 'Film Étirable Manuel 500mm',
    materialType: 'PKG',
    status: 'ACTIVE',
    unitOfMeasure: 'BOX',
    currentStock: 75,
    minimumStock: 35,
    maximumStock: 200,
    reorderPoint: 25,
    safetyStock: 12,
    economicOrderQuantity: 50,
    standardPrice: 48.00,
    standardPriceCurrency: 'EUR',
    costPrice: 36.50,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Protective & Stretch Film',
    supplierName: 'Atlas Packaging & Containers',
    name: 'Heavy Duty Air Bubble Film Roll 1m x 100m',
    description: 'Cushioning 10mm bubble diameter polyethylene protective roll for fragile industrial equipment',
    shortDescription: 'Bubble wrap 1x100m',
    searchKeywords: 'bubble wrap roll protective packing packaging cushion',
    alternativeName: 'Rouleau Film Bulles 1x100m',
    materialType: 'PKG',
    status: 'ACTIVE',
    unitOfMeasure: 'ROLL',
    currentStock: 22,
    minimumStock: 16,
    maximumStock: 80,
    reorderPoint: 12,
    safetyStock: 5,
    economicOrderQuantity: 25,
    standardPrice: 180.00,
    standardPriceCurrency: 'MAD',
    costPrice: 135.00,
    costPriceCurrency: 'MAD',
  },

  // --- Category: Labels, RFID & Identification ---
  {
    categoryName: 'Labels, RFID & Identification',
    supplierName: 'Atlas Packaging & Containers',
    name: 'Thermal Transfer Barcode Labels 100x50mm Roll of 1000',
    description: 'Permanent acrylic adhesive matte white paper labels, 76mm core for Zebra industrial printers',
    shortDescription: 'Labels 100x50mm 1000/roll',
    searchKeywords: 'labels thermal transfer barcode roll Zebra 100x50 sticker',
    alternativeName: 'Étiquettes Code Barre 100x50',
    materialType: 'PKG',
    status: 'ACTIVE',
    unitOfMeasure: 'ROLL',
    currentStock: 140,
    minimumStock: 80,
    maximumStock: 500,
    reorderPoint: 60,
    safetyStock: 30,
    economicOrderQuantity: 100,
    standardPrice: 55.00,
    standardPriceCurrency: 'MAD',
    costPrice: 38.00,
    costPriceCurrency: 'MAD',
  },
  {
    categoryName: 'Labels, RFID & Identification',
    supplierName: 'EuroPack Solutions SAS',
    name: 'RFID UHF On-Metal Tracking Tag 902-928MHz',
    description: 'Ruggedized IP68 mount-on-metal passive RFID asset tag with EPC Gen2 Class 1 chip',
    shortDescription: 'RFID UHF on-metal tag',
    searchKeywords: 'RFID tag UHF metal asset tracking sensor logistics',
    alternativeName: 'Tag RFID Métal UHF',
    materialType: 'CMP',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 450,
    minimumStock: 180,
    maximumStock: 1200,
    reorderPoint: 140,
    safetyStock: 60,
    economicOrderQuantity: 300,
    standardPrice: 2.20,
    standardPriceCurrency: 'EUR',
    costPrice: 1.55,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Precision Hand Tools ---
  {
    categoryName: 'Precision Hand Tools',
    supplierName: 'Apex Industrial Tools Inc',
    name: 'Digital Torque Wrench 1/2" 20-200 Nm',
    description: 'Microprocessor-controlled reversible ratchet with audible buzzer, LED alert, calibration certificate ISO 6789',
    shortDescription: 'Torque wrench 1/2" 200Nm',
    searchKeywords: 'torque wrench digital calibrated ratchet tool 1/2 inch',
    alternativeName: 'Clé Dynamométrique Numérique 20-200Nm',
    materialType: 'TOL',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 15,
    minimumStock: 8,
    maximumStock: 30,
    reorderPoint: 6,
    safetyStock: 3,
    economicOrderQuantity: 10,
    standardPrice: 215.00,
    standardPriceCurrency: 'USD',
    costPrice: 160.00,
    costPriceCurrency: 'USD',
  },
  {
    categoryName: 'Precision Hand Tools',
    supplierName: 'Apex Industrial Tools Inc',
    name: 'ESD-Safe Precision Electronics Screwdriver Set 12-Piece',
    description: 'Dissipative handle Torx, Phillips, Slotted drivers with swivel cap, DIN EN 61340-5-1',
    shortDescription: 'ESD screwdriver set 12pc',
    searchKeywords: 'screwdriver set ESD precision electronics Torx Wiha tool',
    alternativeName: 'Jeu Tournevis Précision ESD 12pcs',
    materialType: 'TOL',
    status: 'ACTIVE',
    unitOfMeasure: 'SET',
    currentStock: 32,
    minimumStock: 14,
    maximumStock: 60,
    reorderPoint: 10,
    safetyStock: 4,
    economicOrderQuantity: 20,
    standardPrice: 58.00,
    standardPriceCurrency: 'USD',
    costPrice: 41.00,
    costPriceCurrency: 'USD',
  },

  // --- Category: Cutting & Machining Tools ---
  {
    categoryName: 'Cutting & Machining Tools',
    supplierName: 'Apex Industrial Tools Inc',
    name: 'Solid Carbide 4-Flute End Mill Ø10mm AlTiN Coated',
    description: 'Micro-grain tungsten carbide milling cutter for stainless and alloy steel, 30° helix, center cutting',
    shortDescription: 'Carbide end mill Ø10mm',
    searchKeywords: 'end mill carbide cutter CNC tooling milling bit 10mm AlTiN',
    alternativeName: 'Fraise Carbure 4 Dents Ø10',
    materialType: 'TOL',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 74,
    minimumStock: 35,
    maximumStock: 180,
    reorderPoint: 25,
    safetyStock: 10,
    economicOrderQuantity: 50,
    standardPrice: 34.00,
    standardPriceCurrency: 'USD',
    costPrice: 22.50,
    costPriceCurrency: 'USD',
  },
  {
    categoryName: 'Cutting & Machining Tools',
    supplierName: 'Apex Industrial Tools Inc',
    name: 'Abrasive Cutting Disc 125x1.0mm Inox Thin',
    description: 'Reinforced ultra-thin metal cut-off wheel for angle grinders, iron/sulfur/chlorine free, box of 25',
    shortDescription: 'Cut disc 125x1mm 25pk',
    searchKeywords: 'cutting disc wheel grinder blade metal inox abrasive 125mm',
    alternativeName: 'Disque Tronçonner Inox 125x1mm',
    materialType: 'CNS',
    status: 'ACTIVE',
    unitOfMeasure: 'BOX',
    currentStock: 160,
    minimumStock: 75,
    maximumStock: 400,
    reorderPoint: 60,
    safetyStock: 25,
    economicOrderQuantity: 100,
    standardPrice: 19.50,
    standardPriceCurrency: 'USD',
    costPrice: 13.00,
    costPriceCurrency: 'USD',
  },

  // --- Category: Measuring & Inspection Instruments ---
  {
    categoryName: 'Measuring & Inspection Instruments',
    supplierName: 'Bavaria Metrology & Sensors AG',
    name: 'Digital Vernier Caliper 150mm IP67 Stainless Steel',
    description: 'Coolant proof electronic caliper, 0.01mm resolution, absolute measurement system, thumb wheel',
    shortDescription: 'Digital caliper 150mm IP67',
    searchKeywords: 'caliper vernier digital gauge micrometer measuring tool 150mm',
    alternativeName: 'Pied à Coulisse Digital 150mm',
    materialType: 'TOL',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 28,
    minimumStock: 12,
    maximumStock: 50,
    reorderPoint: 10,
    safetyStock: 4,
    economicOrderQuantity: 15,
    standardPrice: 145.00,
    standardPriceCurrency: 'EUR',
    costPrice: 108.00,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Measuring & Inspection Instruments',
    supplierName: 'Bavaria Metrology & Sensors AG',
    name: 'Dual Laser Infrared Thermometer -50°C to +800°C',
    description: 'Non-contact optical pyrometer 12:1 distance-to-spot ratio, adjustable emissivity, color LCD display',
    shortDescription: 'IR Thermometer 800°C',
    searchKeywords: 'infrared thermometer pyrometer laser heat sensor inspection',
    alternativeName: 'Thermomètre Infrarouge Laser',
    materialType: 'TOL',
    status: 'BLOCKED', // BLOCKED test case!
    unitOfMeasure: 'PCE',
    currentStock: 6,
    minimumStock: 8, // Below minimum! (6 < 8)
    maximumStock: 25,
    reorderPoint: 6,
    safetyStock: 2,
    economicOrderQuantity: 10,
    standardPrice: 89.00,
    standardPriceCurrency: 'EUR',
    costPrice: 65.00,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Industrial Adhesives & Sealants ---
  {
    categoryName: 'Industrial Adhesives & Sealants',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'Two-Component Structural Epoxy Adhesive 50ml Duo-Cartridge',
    description: 'High shear strength toughened epoxy adhesive, 20-minute working life, bond gap filling up to 3mm',
    shortDescription: 'Epoxy adhesive 50ml cartridge',
    searchKeywords: 'epoxy adhesive glue resin structural bonding 2-part chemical',
    alternativeName: 'Colle Époxy Bicomposant 50ml',
    materialType: 'CHM',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 195,
    minimumStock: 80,
    maximumStock: 400,
    reorderPoint: 60,
    safetyStock: 25,
    economicOrderQuantity: 120,
    standardPrice: 18.20,
    standardPriceCurrency: 'EUR',
    costPrice: 12.80,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Industrial Adhesives & Sealants',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'Medium-Strength Threadlocking Compound 50ml Bottle',
    description: 'Anaerobic thread locker for metric screws up to M36, prevents vibration loosening, oil tolerant',
    shortDescription: 'Threadlocker blue 50ml',
    searchKeywords: 'threadlocker Loctite adhesive screw lock anaerobic chemical',
    alternativeName: 'Frein Filet Moyen 50ml',
    materialType: 'CHM',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 85,
    minimumStock: 40,
    maximumStock: 200,
    reorderPoint: 30,
    safetyStock: 15,
    economicOrderQuantity: 50,
    standardPrice: 22.50,
    standardPriceCurrency: 'EUR',
    costPrice: 15.60,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Cleaning & Degreasing Solvents ---
  {
    categoryName: 'Cleaning & Degreasing Solvents',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'High-Purity Isopropyl Alcohol 99.9% 5-Liter Canister',
    description: 'Electronics wash grade IPA, residue-free rapid evaporation solvent for PCB flux removal and optics',
    shortDescription: 'IPA 99.9% 5L canister',
    searchKeywords: 'isopropyl alcohol IPA solvent cleaner degreaser chemicals 5L',
    alternativeName: 'Alcool Isopropylique 99.9% 5L',
    materialType: 'CHM',
    status: 'ACTIVE',
    unitOfMeasure: 'L',
    currentStock: 120,
    minimumStock: 60,
    maximumStock: 300,
    reorderPoint: 45,
    safetyStock: 20,
    economicOrderQuantity: 80,
    standardPrice: 32.00,
    standardPriceCurrency: 'EUR',
    costPrice: 23.00,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Cleaning & Degreasing Solvents',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'Heavy-Duty Aerosol Brake & Machinery Cleaner 500ml',
    description: 'Fast drying chlorine-free degreaser spray, high pressure nozzle for flushing oil, grease, and grime',
    shortDescription: 'Degreaser spray 500ml',
    searchKeywords: 'cleaner degreaser aerosol spray brake parts solvent',
    alternativeName: 'Dégraissant Pièces Aérosol 500ml',
    materialType: 'CNS',
    status: 'INACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 12, // Below minimum! (12 < 30)
    minimumStock: 30,
    maximumStock: 150,
    reorderPoint: 20,
    safetyStock: 8,
    economicOrderQuantity: 40,
    standardPrice: 6.80,
    standardPriceCurrency: 'EUR',
    costPrice: 4.50,
    costPriceCurrency: 'EUR',
  },

  // --- Category: Lubricants & Industrial Oils ---
  {
    categoryName: 'Lubricants & Industrial Oils',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'Fully Synthetic Industrial Gear Oil ISO VG 220 20L Drum',
    description: 'PAO-based extreme pressure lubricant for heavily loaded industrial enclosed gearboxes and drives',
    shortDescription: 'Gear oil VG220 20L drum',
    searchKeywords: 'gear oil synthetic lubricant ISO VG 220 industrial oil drum',
    alternativeName: 'Huile Engrenage Synthétique 20L',
    materialType: 'CHM',
    status: 'ACTIVE',
    unitOfMeasure: 'DRUM',
    currentStock: 14,
    minimumStock: 8,
    maximumStock: 40,
    reorderPoint: 6,
    safetyStock: 3,
    economicOrderQuantity: 12,
    standardPrice: 165.00,
    standardPriceCurrency: 'EUR',
    costPrice: 125.00,
    costPriceCurrency: 'EUR',
  },
  {
    categoryName: 'Lubricants & Industrial Oils',
    supplierName: 'Rhine Specialty Chemicals GmbH',
    name: 'High-Temperature PTFE Bearing Grease 400g Cartridge',
    description: 'Multi-purpose lithium complex grease with micronized Teflon particles, operating range -30°C to +180°C',
    shortDescription: 'PTFE grease 400g tube',
    searchKeywords: 'grease bearing PTFE teflon lubricant cartridge high temperature',
    alternativeName: 'Graisse Roulement PTFE 400g',
    materialType: 'CNS',
    status: 'ACTIVE',
    unitOfMeasure: 'PCE',
    currentStock: 185,
    minimumStock: 70,
    maximumStock: 400,
    reorderPoint: 55,
    safetyStock: 25,
    economicOrderQuantity: 100,
    standardPrice: 8.50,
    standardPriceCurrency: 'EUR',
    costPrice: 5.80,
    costPriceCurrency: 'EUR',
  },
];

// ============================================================
// HELPER UTILITIES
// ============================================================

async function fetchJson(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {}),
    },
  });

  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch (e) {
    data = text;
  }

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || (typeof data === 'string' ? data : `HTTP ${response.status}`);
    const err = new Error(errorMsg);
    err.status = response.status;
    err.body = data;
    throw err;
  }

  return data;
}

// ============================================================
// SEEDING FUNCTIONS
// ============================================================

async function seedCategories() {
  console.log('\n📂 ========================================');
  console.log('📂 1. SEEDING CATEGORIES (Roots & Children)');
  console.log('📂 ========================================');

  // Query existing categories to avoid duplicates
  let existingCategories = [];
  try {
    existingCategories = await fetchJson(`${BASE_URL}/categories`);
  } catch (err) {
    console.warn('⚠️ Could not fetch existing categories, proceeding directly:', err.message);
  }

  const categoryMap = new Map(); // name -> id
  if (Array.isArray(existingCategories)) {
    for (const c of existingCategories) {
      if (c.name && c.id) {
        categoryMap.set(c.name.toLowerCase().trim(), c.id);
      }
    }
  }

  let createdRoots = 0;
  let skippedRoots = 0;

  // 1.1 Create Root Categories
  for (const root of ROOT_CATEGORIES) {
    const key = root.name.toLowerCase().trim();
    if (categoryMap.has(key)) {
      console.log(`  ⏭️  [ROOT EXISTS] ${root.name} (id: ${categoryMap.get(key)})`);
      skippedRoots++;
      continue;
    }

    try {
      const payload = {
        name: root.name,
        description: root.description,
        shortDescription: root.shortDescription,
        parentId: null,
        categoryType: root.categoryType,
        status: root.status,
        createdBy: CREATED_BY,
      };

      const res = await fetchJson(`${BASE_URL}/categories`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      categoryMap.set(key, res.id);
      console.log(`  ✅ [ROOT CREATED] ${root.name} -> Code: ${res.code} (id: ${res.id})`);
      createdRoots++;
    } catch (err) {
      console.error(`  ❌ [FAILED] Root ${root.name}:`, err.message);
    }
  }

  let createdChildren = 0;
  let skippedChildren = 0;

  // 1.2 Create Child Categories
  for (const child of CHILD_CATEGORIES) {
    const key = child.name.toLowerCase().trim();
    if (categoryMap.has(key)) {
      console.log(`  ⏭️  [CHILD EXISTS] ${child.name} (id: ${categoryMap.get(key)})`);
      skippedChildren++;
      continue;
    }

    const parentKey = child.parentName.toLowerCase().trim();
    const parentId = categoryMap.get(parentKey);
    if (!parentId) {
      console.error(`  ❌ [FAILED] Child ${child.name}: Parent '${child.parentName}' not found in map!`);
      continue;
    }

    try {
      const payload = {
        name: child.name,
        description: child.description,
        shortDescription: child.shortDescription,
        parentId: parentId,
        categoryType: child.categoryType,
        status: child.status,
        createdBy: CREATED_BY,
      };

      const res = await fetchJson(`${BASE_URL}/categories`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      categoryMap.set(key, res.id);
      console.log(`  ✅ [CHILD CREATED] ${child.name} (parent: ${child.parentName}) -> Code: ${res.code} (id: ${res.id})`);
      createdChildren++;
    } catch (err) {
      console.error(`  ❌ [FAILED] Child ${child.name}:`, err.message);
    }
  }

  console.log(`\n📂 Categories summary: ${createdRoots + createdChildren} created (${createdRoots} roots, ${createdChildren} children), ${skippedRoots + skippedChildren} skipped.`);
  return categoryMap;
}

async function seedSuppliers() {
  console.log('\n🏭 ========================================');
  console.log('🏭 2. SEEDING SUPPLIERS');
  console.log('🏭 ========================================');

  let existingSuppliers = [];
  try {
    const raw = await fetchJson(`${BASE_URL}/suppliers`);
    existingSuppliers = Array.isArray(raw) ? raw : (raw?.content || []);
  } catch (err) {
    console.warn('⚠️ Could not fetch existing suppliers, proceeding directly:', err.message);
  }

  const supplierMap = new Map(); // name -> id
  if (Array.isArray(existingSuppliers)) {
    for (const s of existingSuppliers) {
      if (s.name && s.id) {
        supplierMap.set(s.name.toLowerCase().trim(), s.id);
      }
    }
  }

  let created = 0;
  let skipped = 0;

  for (const s of SUPPLIERS) {
    const key = s.name.toLowerCase().trim();
    if (supplierMap.has(key)) {
      console.log(`  ⏭️  [EXISTS] ${s.name} (id: ${supplierMap.get(key)})`);
      skipped++;
      continue;
    }

    try {
      const payload = {
        name: s.name,
        description: s.description,
        contactPerson: s.contactPerson,
        contactEmail: s.contactEmail,
        contactPhone: s.contactPhone,
        address: s.address,
        city: s.city,
        country: s.country,
        postalCode: s.postalCode,
        paymentTerms: s.paymentTerms,
        currencyCode: s.currencyCode,
        createdBy: CREATED_BY,
      };

      const res = await fetchJson(`${BASE_URL}/suppliers`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      supplierMap.set(key, res.id);
      console.log(`  ✅ [CREATED] ${s.name} (${s.country}, ${s.currencyCode}) -> Code: ${res.code} (id: ${res.id})`);
      created++;
    } catch (err) {
      console.error(`  ❌ [FAILED] Supplier ${s.name}:`, err.message);
    }
  }

  console.log(`\n🏭 Suppliers summary: ${created} created, ${skipped} skipped.`);
  return supplierMap;
}

async function seedMaterials(categoryMap, supplierMap) {
  console.log('\n📦 ========================================');
  console.log('📦 3. SEEDING MATERIALS');
  console.log('📦 ========================================');

  let existingMaterials = [];
  try {
    const raw = await fetchJson(`${BASE_URL}/materials`);
    existingMaterials = Array.isArray(raw) ? raw : (raw?.content || []);
  } catch (err) {
    console.warn('⚠️ Could not fetch existing materials, proceeding directly:', err.message);
  }

  const materialNames = new Set();
  if (Array.isArray(existingMaterials)) {
    for (const m of existingMaterials) {
      if (m.name) {
        materialNames.add(m.name.toLowerCase().trim());
      }
    }
  }

  let created = 0;
  let skipped = 0;

  for (const m of MATERIALS) {
    const key = m.name.toLowerCase().trim();
    if (materialNames.has(key)) {
      console.log(`  ⏭️  [EXISTS] ${m.name}`);
      skipped++;
      continue;
    }

    const categoryId = categoryMap.get(m.categoryName.toLowerCase().trim());
    if (!categoryId) {
      console.error(`  ❌ [FAILED] Material '${m.name}': Category '${m.categoryName}' not found in map!`);
      continue;
    }

    const supplierId = supplierMap.get(m.supplierName.toLowerCase().trim());
    if (!supplierId) {
      console.error(`  ❌ [FAILED] Material '${m.name}': Supplier '${m.supplierName}' not found in map!`);
      continue;
    }

    try {
      const payload = {
        name: m.name,
        description: m.description,
        shortDescription: m.shortDescription,
        searchKeywords: m.searchKeywords,
        alternativeName: m.alternativeName,
        categoryId: categoryId,
        supplierId: supplierId,
        materialType: m.materialType,
        status: m.status,
        unitOfMeasure: m.unitOfMeasure,
        currentStock: m.currentStock,
        minimumStock: m.minimumStock,
        maximumStock: m.maximumStock,
        reorderPoint: m.reorderPoint,
        safetyStock: m.safetyStock,
        economicOrderQuantity: m.economicOrderQuantity,
        standardPrice: m.standardPrice,
        standardPriceCurrency: m.standardPriceCurrency,
        costPrice: m.costPrice,
        costPriceCurrency: m.costPriceCurrency,
        createdBy: CREATED_BY,
      };

      const res = await fetchJson(`${BASE_URL}/materials`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      console.log(`  ✅ [CREATED] [${m.status}] ${m.name} (${m.unitOfMeasure}, Stock: ${m.currentStock}, ${m.standardPrice} ${m.standardPriceCurrency}) -> Code: ${res.code}`);
      created++;
    } catch (err) {
      console.error(`  ❌ [FAILED] Material '${m.name}':`, err.message);
    }
  }

  console.log(`\n📦 Materials summary: ${created} created, ${skipped} skipped.`);
}

// ============================================================
// MAIN EXECUTION
// ============================================================

async function main() {
  const startTime = Date.now();
  console.log('====================================================');
  console.log('🌱 Starting Materia Test Data Seeder');
  console.log(`🎯 Target API Base: ${BASE_URL}`);
  console.log('====================================================');

  try {
    // Step 1: Categories
    const categoryMap = await seedCategories();

    // Step 2: Suppliers
    const supplierMap = await seedSuppliers();

    // Step 3: Materials
    await seedMaterials(categoryMap, supplierMap);

    const elapsed = ((Date.now() - startTime) / 1000).toFixed(2);
    console.log('\n====================================================');
    console.log(`🎉 TEST DATA SEEDING COMPLETE in ${elapsed}s!`);
    console.log('====================================================\n');
  } catch (err) {
    console.error('\n❌ Unhandled error during test data seeding:', err);
    process.exit(1);
  }
}

main();
