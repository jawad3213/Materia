import axiosClient from "../../../shared/api/axiosClient";
import { withChangeReason } from "../../audit/services/auditService";
import type { AppSetting } from "../types";

const BASE_URL = "/settings";

const settingsService = {
  /** Every setting with its current value (setting:read). */
  getAll: () => axiosClient.get<AppSetting[]>(BASE_URL),

  /** Saves the changed values (setting:write); the reason goes into the history. */
  update: (values: Record<string, string>, reason?: string) =>
    axiosClient.put<AppSetting[]>(BASE_URL, values, withChangeReason(reason)),
};

export default settingsService;
