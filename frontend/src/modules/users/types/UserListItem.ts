import type { EmploymentStatusValue } from '../enums/EmploymentStatus';
import type { UserRoleValue } from '../enums/UserRole';

/**
 * Representation of an employee / user item in the list view table
 */
export interface UserListItem {
  id: string;
  fullName: string;
  email: string;
  code: string;
  status: EmploymentStatusValue;
  role?: UserRoleValue;
  createdAt: string;
  userId?: string;
}

export type EmployeeListItem = UserListItem;
