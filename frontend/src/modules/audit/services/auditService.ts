import axiosClient from "../../../shared/api/axiosClient";
import type { AuditEntityType, AuditEntry } from "../types";

const BASE_URL = "/audit";

/** Header the backend records as the reason of every change made by the request. */
export const CHANGE_REASON_HEADER = "X-Change-Reason";

const auditService = {
  /** What happened to a record and its lines, newest first. */
  history: (entityType: AuditEntityType, entityId: string) =>
    axiosClient.get<AuditEntry[]>(`${BASE_URL}/${entityType}/${entityId}`),

  /** The latest changes across the platform (needs log:read). */
  recent: (params: { entityType?: string; actorId?: string; limit?: number } = {}) =>
    axiosClient.get<AuditEntry[]>(BASE_URL, { params }),

  /** The same feed as a CSV file (needs log:export). */
  exportCsv: (params: { entityType?: string; actorId?: string; limit?: number } = {}) =>
    axiosClient.get<Blob>(`${BASE_URL}/export`, { params, responseType: "blob" }),
};

/** Request options that record {@code reason} as the reason of the changes the request makes. */
export const withChangeReason = (reason?: string) =>
  reason && reason.trim() ? { headers: { [CHANGE_REASON_HEADER]: encodeURIComponent(reason.trim()) } } : {};

export default auditService;
