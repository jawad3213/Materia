export const MaterialType = {
  RAW_MATERIAL: 'RMT',
  FINISHED_GOOD: 'FGD',
  COMPONENT: 'CMP',
  PACKAGING: 'PKG',
  SPARE_PART: 'SPR',
  CONSUMABLE: 'CNS',
  SERVICE: 'SRV',
  TOOL: 'TOL',
  CHEMICAL: 'CHM',
  ELECTRONIC: 'ELC',
} as const;

export type MaterialTypeValue = typeof MaterialType[keyof typeof MaterialType];
