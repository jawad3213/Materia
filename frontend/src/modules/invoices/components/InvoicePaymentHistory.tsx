import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import paymentService from "../../payments/services/paymentService";
import PaymentStatusBadge from "../../payments/components/PaymentStatusBadge";
import { PAYMENT_METHOD_LABELS, type Payment } from "../../payments/types/payment.types";
import type { Invoice } from "../types/invoice.types";
import { formatAmount } from "../utils/invoiceLine";
import useAuth from "../../auth/hooks/useAuth";
import { DetailCard } from "../../../shared/components/page/DetailParts";
import { SectionIcons } from "../../../shared/components/page/pageIcons";

/** The payments that cover this invoice (open ones set money aside; completed ones were recorded on it). */
export default function InvoicePaymentHistory({ invoice, refreshKey }: { invoice: Invoice; refreshKey?: unknown }) {
  const { hasPermission } = useAuth();
  const canRead = hasPermission("payment:read");
  const [payments, setPayments] = useState<Payment[] | null>(null);

  useEffect(() => {
    if (!canRead) return;
    let cancelled = false;
    paymentService
      .getBySupplierId(invoice.supplierId)
      .then((res) => {
        if (!cancelled) setPayments(res.data.filter((p) => p.lines.some((l) => l.invoiceId === invoice.id)));
      })
      .catch(() => {
        if (!cancelled) setPayments([]);
      });
    return () => {
      cancelled = true;
    };
  }, [canRead, invoice.id, invoice.supplierId, refreshKey]);

  if (!canRead || payments === null) return null;

  return (
    <DetailCard title="Payments" icon={SectionIcons.money} tone="success">
      {payments.length === 0 ? (
        <p className="text-sm text-gray-500 dark:text-gray-400">No payment for this invoice yet.</p>
      ) : (
        <ul className="divide-y divide-gray-100 dark:divide-white/[0.05]">
          {payments.map((p) => {
            const line = p.lines.find((l) => l.invoiceId === invoice.id);
            return (
              <li key={p.id} className="flex flex-wrap items-center justify-between gap-2 py-3 first:pt-0 last:pb-0">
                <span className="flex items-center gap-3">
                  <Link to={`/payments/${p.id}`} className="font-mono text-sm font-medium text-brand-500 hover:underline">
                    {p.paymentCode}
                  </Link>
                  <PaymentStatusBadge status={p.status} />
                </span>
                <span className="text-theme-sm text-gray-600 dark:text-gray-400">
                  {formatAmount(line?.amount ?? 0, invoice.currencyCode)}
                  {p.paymentMethod && ` • ${PAYMENT_METHOD_LABELS[p.paymentMethod]}`}
                  {p.confirmedDate && ` • ${p.confirmedDate.slice(0, 10)}`}
                </span>
              </li>
            );
          })}
        </ul>
      )}
    </DetailCard>
  );
}
