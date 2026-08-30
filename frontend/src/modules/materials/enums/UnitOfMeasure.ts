export const UnitOfMeasure = {
  // MASS / WEIGHT
  KG: 'KG',
  G: 'G',
  T: 'T',
  MG: 'MG',
  LB: 'LB',
  OZ: 'OZ',
  // VOLUME / LIQUIDS
  L: 'L',
  ML: 'ML',
  M3: 'M3',
  DM3: 'DM3',
  CM3: 'CM3',
  GAL: 'GAL',
  FT3: 'FT3',
  // LENGTH / DISTANCE
  M: 'M',
  CM: 'CM',
  MM: 'MM',
  KM: 'KM',
  IN: 'IN',
  FT: 'FT',
  YD: 'YD',
  // AREA
  M2: 'M2',
  CM2: 'CM2',
  MM2: 'MM2',
  HA: 'HA',
  ACRE: 'ACRE',
  // COUNT / UNITS
  PCE: 'PCE',
  BOX: 'BOX',
  CART: 'CART',
  PACK: 'PACK',
  SET: 'SET',
  PAL: 'PAL',
  DRUM: 'DRUM',
  ROLL: 'ROLL',
  SHEET: 'SHEET',
  REEL: 'REEL',
  // TIME
  HOUR: 'HOUR',
  DAY: 'DAY',
  WEEK: 'WEEK',
  MONTH: 'MONTH',
  // ENERGY / POWER
  KWH: 'KWH',
  KW: 'KW',
  HP: 'HP',
  // TEMPERATURE
  C: 'C',
  F: 'F',
  K: 'K',
  // PRESSURE
  BAR: 'BAR',
  PSI: 'PSI',
  PA: 'PA',
  ATM: 'ATM',
  // OTHER
  NONE: 'NONE',
} as const;

export type UnitOfMeasureValue = typeof UnitOfMeasure[keyof typeof UnitOfMeasure];
