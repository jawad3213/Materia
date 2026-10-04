import useAuth from "../../auth/hooks/useAuth";
import type { ReturnToVendor } from "../types/returnToVendor.types";

/**
 * Action availability for returns, following Role.java (`return:write`) and the backend lifecycle:
 * a draft is shipped (submitted) or deleted, a shipped return is resolved, and either can be cancelled.
 */
export default function useReturnToVendorPermissions() {
  const { hasPermission } = useAuth();
  const canWrite = hasPermission("return:write");

  return {
    canCreate: canWrite,
    canSubmit: (rtv: ReturnToVendor) => canWrite && rtv.status === "DRAFT",
    canResolve: (rtv: ReturnToVendor) => canWrite && rtv.status === "PENDING",
    canCancel: (rtv: ReturnToVendor) => canWrite && (rtv.status === "DRAFT" || rtv.status === "PENDING"),
    canDelete: (rtv: ReturnToVendor) => canWrite && rtv.status === "DRAFT",
  };
}
