import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionListTable from "../components/RequisitionListTable";

export default function RequisitionsPage() {
  return (
    <>
      <PageMeta title="Requisitions | Materia Dashboard" description="Purchase requisitions, their approval and their conversion into orders" />
      <PageBreadcrumb pageTitle="Requisitions" />
      <div className="space-y-6">
        <RequisitionListTable />
      </div>
    </>
  );
}
