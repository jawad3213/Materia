import PageMeta from "../../../shared/components/common/PageMeta";
import PaymentDetail from "../components/PaymentDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function PaymentDetailPage() {
  return (
    <>
      <PageMeta title="Payment | Materia Dashboard" description="Invoices paid, method and bank reference of the payment" />
      <PaymentDetail />
    </>
  );
}
