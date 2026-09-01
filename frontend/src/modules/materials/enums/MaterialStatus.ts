export const MaterialStatus = {
  DRAFT: 'DRAFT',
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  BLOCKED: 'BLOCKED',
  OBSOLETE: 'OBSOLETE',
} as const;

export type MaterialStatusValue = typeof MaterialStatus[keyof typeof MaterialStatus];
