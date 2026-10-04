/**
 * Segregation of duties, mirroring backend Requisition.approve: neither the requester nor whoever
 * created the requisition may approve it.
 */
export function isOwnRequisition(
  requisition: { requesterId?: string | null; createdBy?: string | null },
  userId?: string | null
): boolean {
  if (!userId) return false;
  return requisition.requesterId === userId || requisition.createdBy === userId;
}
