import { useCallback, useEffect, useState } from "react";
import dashboardService from "../services/dashboardService";
import type { DashboardData } from "../types/dashboard.types";
import { getApiErrorMessage } from "../../../shared/utils/apiError";

/** Loads the dashboard; `refresh` reloads it on demand. */
export default function useDashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let cancelled = false;
    dashboardService
      .getOverview()
      .then((res) => {
        if (!cancelled) {
          setData(res.data);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Could not load the dashboard."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [version]);

  const refresh = useCallback(() => {
    setLoading(true);
    setVersion((v) => v + 1);
  }, []);

  return { data, loading, error, refresh };
}
