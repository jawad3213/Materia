export const SupplierStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
} as const;

export type SupplierStatusValue = typeof SupplierStatus[keyof typeof SupplierStatus];
