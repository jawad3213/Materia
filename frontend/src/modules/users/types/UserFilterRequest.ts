import type { EmploymentStatusValue } from '../enums/EmploymentStatus';

/**
 * Filter criteria for users / employees
 */
export interface UserFilterRequest {
  status?: EmploymentStatusValue;
  search?: string;
}

export type EmployeeFilterRequest = UserFilterRequest;
