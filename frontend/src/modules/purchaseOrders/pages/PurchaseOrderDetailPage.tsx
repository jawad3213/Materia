import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderDetail from "../components/PurchaseOrderDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function PurchaseOrderDetailPage() {
  return (
    <>
      <PageMeta title="Purchase Order | Materia Dashboard" description="Order lines, terms and workflow of a purchase order" />
      <PurchaseOrderDetail />
    </>
  );
}
