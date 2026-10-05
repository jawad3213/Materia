import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionApprovalsTable from "../components/RequisitionApprovalsTable";

export default function RequisitionApprovalsPage() {
  return (
    <>
      <PageMeta title="Approvals | Materia Dashboard" description="Review and approve or reject submitted requisitions" />
      <PageBreadcrumb pageTitle="Approvals" parentName="Requisitions" parentUrl="/requisitions" />
      <div className="space-y-6">
        <RequisitionApprovalsTable />
      </div>
    </>
  );
}
