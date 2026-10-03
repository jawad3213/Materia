/**
 * The shared action matrix: who may do what, in which status (spec 002, research R8).
 *
 * The backend tests assert against the same file, so a rule changed on one side only fails the
 * other (SC-002a). This is the single import site; if the file moves, change MATRIX_PATH's import.
 * Only tests import this module, and production builds (`vite build`) never reach it.
 */
import matrixJson from "../../../backend/src/test/resources/contracts/purchase-order-actions.json";

/** Kept for error messages; the import above must use the same path. */
export const MATRIX_PATH = "backend/src/test/resources/contracts/purchase-order-actions.json";

export type Aggregate = "purchaseOrder" | "goodsReceipt";
export type MatrixRole = "ADMIN" | "PURCHASER" | "RECEIVER";

interface ActionRule {
  permission: string;
  statuses: string[];
  requiresRole?: string;
  requiresAssignedReceiver?: boolean;
  requiresOwnReceipt?: boolean;
}

interface RoleRule {
  passesEveryPermissionCheck: boolean;
  permissions: string[];
}

interface Matrix {
  roles: Record<MatrixRole, RoleRule>;
  purchaseOrder: { statuses: string[]; actions: Record<string, ActionRule> };
  goodsReceipt: { statuses: string[]; actions: Record<string, ActionRule> };
}

export const matrix = matrixJson as unknown as Matrix;

export const roles = Object.keys(matrix.roles) as MatrixRole[];

export function statuses(aggregate: Aggregate): string[] {
  return matrix[aggregate].statuses;
}

export function rule(aggregate: Aggregate, action: string): ActionRule {
  const found = matrix[aggregate].actions[action];
  if (!found) {
    throw new Error(`Unknown action ${aggregate}.${action} in ${MATRIX_PATH}`);
  }
  return found;
}

/** Same semantics as the backend's ActionMatrix.isAllowed. */
export function isAllowed(
  aggregate: Aggregate,
  role: MatrixRole,
  status: string,
  action: string,
  ownsRecord: boolean
): boolean {
  const r = rule(aggregate, action);
  const roleRule = matrix.roles[role];
  const hasPermission = roleRule.passesEveryPermissionCheck || roleRule.permissions.includes(r.permission);
  const hasRole = !r.requiresRole || r.requiresRole === role;
  const statusOk = r.statuses.length === 0 || r.statuses.includes(status);
  const needsOwnership = !!r.requiresAssignedReceiver || !!r.requiresOwnReceipt;
  return hasPermission && hasRole && statusOk && (!needsOwnership || ownsRecord);
}
