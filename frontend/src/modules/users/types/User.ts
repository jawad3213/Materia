import type { EmploymentStatusValue } from '../enums/EmploymentStatus';
import type { UserRoleValue } from '../enums/UserRole';

/**
 * Core User / Employee entity interface matching backend EmployeeWebResponse
 */
export interface UserItem {
  id: string; // UUID
  code: string; // e.g. "EMP-2026-0001"
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone?: string;
  status: EmploymentStatusValue;
  userId?: string; // Associated system user ID in auth module
  role?: UserRoleValue;
  hireDate?: string; // ISO date YYYY-MM-DD
  terminationDate?: string;
  terminationReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export type Employee = UserItem;
