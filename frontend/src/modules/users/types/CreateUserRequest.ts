import type { EmploymentStatusValue } from '../enums/EmploymentStatus';

/**
 * Payload for creating an employee without provisioning credentials
 */
export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  hireDate?: string;
  status?: EmploymentStatusValue;
}

export type CreateEmployeeRequest = CreateUserRequest;
