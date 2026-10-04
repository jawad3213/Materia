import PageMeta from "../../../shared/components/common/PageMeta";
import ReturnDetail from "../components/ReturnDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function ReturnDetailPage() {
  return (
    <>
      <PageMeta title="Return | Materia Dashboard" description="Returned goods, shipment and resolution by the supplier" />
      <ReturnDetail />
    </>
  );
}
