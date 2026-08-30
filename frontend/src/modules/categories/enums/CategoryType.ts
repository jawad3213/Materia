export const CategoryType = {
  MATERIAL: 'MAT',
  PRODUCT: 'PRD',
  SERVICE: 'SRV',
  RAW_MATERIAL_CAT: 'RMC',
  COMPONENT_CAT: 'CMP',
  PACKAGING_CAT: 'PKG',
  SPARE_PART_CAT: 'SPR',
  CONSUMABLE_CAT: 'CNS',
  TOOL_CAT: 'TOL',
  CHEMICAL_CAT: 'CHM',
  ELECTRONIC_CAT: 'ELC',
  FAMILY: 'FAM',
  BRAND: 'BRD',
  DEPARTMENT: 'DEP',
  PROJECT: 'PRJ',
  GEOGRAPHIC: 'GEO',
  SEASONAL: 'SEA',
} as const;

export type CategoryTypeValue = typeof CategoryType[keyof typeof CategoryType];
