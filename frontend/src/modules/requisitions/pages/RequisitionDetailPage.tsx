import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionDetail from "../components/RequisitionDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function RequisitionDetailPage() {
  return (
    <>
      <PageMeta title="Requisition | Materia Dashboard" description="Requested lines, review and lifecycle of a requisition" />
      <RequisitionDetail />
    </>
  );
}
