import { useMemo, useState } from "react";

/** Client-side paging for lists loaded in full. Resets to the first page when the items change length. */
export function useClientPagination<T>(items: T[], size = 10) {
  const [page, setPage] = useState(0);
  const totalPages = Math.max(1, Math.ceil(items.length / size));
  const current = Math.min(page, totalPages - 1);
  const pageItems = useMemo(() => items.slice(current * size, current * size + size), [items, current, size]);
  return { page: current, setPage, size, totalPages, total: items.length, pageItems };
}
