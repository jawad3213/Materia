export interface OffboardUserRequest {
  employeeId: string;
  terminationDate?: string; // YYYY-MM-DD
  reason?: string;
  revokeUserAccess?: boolean;
}

export type OffboardEmployeeRequest = OffboardUserRequest;

