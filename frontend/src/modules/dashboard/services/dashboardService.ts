import axiosClient from "../../../shared/api/axiosClient";
import type { DashboardData } from "../types/dashboard.types";

/** The backend builds the dashboard from the caller's own permissions. */
export const dashboardService = {
  getOverview: () => axiosClient.get<DashboardData>("/dashboard"),
};

export default dashboardService;
