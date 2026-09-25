import type { EmploymentStatusValue } from '../enums/EmploymentStatus';

/**
 * Payload for updating an existing employee's details
 */
export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  status?: EmploymentStatusValue;
}

export type UpdateEmployeeRequest = UpdateUserRequest;
