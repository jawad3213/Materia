import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceDetail from "../components/InvoiceDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function InvoiceDetailPage() {
  return (
    <>
      <PageMeta title="Invoice | Materia Dashboard" description="Invoice matched against its order and receipts, verification and payment" />
      <InvoiceDetail />
    </>
  );
}
