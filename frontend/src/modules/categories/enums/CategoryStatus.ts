export const CategoryStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

export type CategoryStatusValue = typeof CategoryStatus[keyof typeof CategoryStatus];
