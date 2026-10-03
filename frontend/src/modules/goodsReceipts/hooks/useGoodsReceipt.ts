import useAuth from "../../auth/hooks/useAuth";
import { OPEN_RECEIPT_STATUSES, type GoodsReceipt } from "../types/goodsReceipt.types";

/**
 * Action availability for goods receipts. Only the receiver assigned to the purchase order may
 * record, validate or cancel its receipts; the backend enforces the same rule.
 */
export default function useGoodsReceiptPermissions() {
  const { user, hasPermission } = useAuth();
  const canWrite = hasPermission("receipt:write");

  const isOwnOpenReceipt = (receipt: GoodsReceipt) =>
    canWrite && !!user?.id && receipt.receivedBy === user.id && OPEN_RECEIPT_STATUSES.includes(receipt.status);

  return {
    canRecord: canWrite,
    canComplete: isOwnOpenReceipt,
    canCancel: isOwnOpenReceipt,
    canDelete: isOwnOpenReceipt,
  };
}
