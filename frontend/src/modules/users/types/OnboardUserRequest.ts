import type { EmploymentStatusValue } from '../enums/EmploymentStatus';
import type { UserRoleValue } from '../enums/UserRole';

/**
 * Payload for onboarding an employee and optionally provisioning system credentials
 */
export interface OnboardUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  hireDate?: string;
  status?: EmploymentStatusValue;
  provisionCredentials: boolean;
  roleCode?: UserRoleValue;
  initialPassword?: string;
}

export type OnboardEmployeeRequest = OnboardUserRequest;
