import useAuth from "../../auth/hooks/useAuth";
import { MODIFIABLE_INVOICE_STATUSES, type Invoice } from "../types/invoice.types";
import { isPartiallyPaid } from "../utils/invoiceLine";

/**
 * Action availability for invoices, following the permissions in Role.java:
 * purchasers record and submit invoices (`invoice:write`), administrators verify them
 * (`invoice:validate`) and pay them (`payment:write`).
 */
export default function useInvoicePermissions() {
  const { hasPermission, user } = useAuth();
  const isAdmin = user?.role === "ADMIN";
  const canWrite = hasPermission("invoice:write");
  const canValidate = hasPermission("invoice:validate");
  const canPayInvoices = hasPermission("payment:write");

  return {
    canRecord: isAdmin && canWrite,
    canSubmit: (invoice: Invoice) => canWrite && invoice.status === "DRAFT",
    canVerify: (invoice: Invoice) => canValidate && invoice.status === "SUBMITTED",
    canPay: (invoice: Invoice) => canPayInvoices && invoice.status === "VERIFIED",
    // Once a payment is recorded the invoice can no longer be cancelled (backend Invoice.cancel).
    canCancel: (invoice: Invoice) =>
      invoice.status === "VERIFIED"
        ? canValidate && !isPartiallyPaid(invoice)
        : canWrite && MODIFIABLE_INVOICE_STATUSES.includes(invoice.status),
    canDelete: (invoice: Invoice) => canWrite && invoice.status === "DRAFT",
  };
}
