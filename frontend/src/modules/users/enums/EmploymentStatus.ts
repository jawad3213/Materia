export const EmploymentStatus = {
  ACTIVE: 'ACTIVE',
  PROBATION: 'PROBATION',
  ON_LEAVE: 'ON_LEAVE',
  SUSPENDED: 'SUSPENDED',
  TERMINATED: 'TERMINATED',
} as const;

export type EmploymentStatusValue = (typeof EmploymentStatus)[keyof typeof EmploymentStatus];
