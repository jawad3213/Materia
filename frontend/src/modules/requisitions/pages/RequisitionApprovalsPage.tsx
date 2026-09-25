import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionApprovalsTable from "../components/RequisitionApprovalsTable";

export default function RequisitionApprovalsPage() {
  return (
    <>
      <PageMeta
        title="Requisition Approvals Portal | Materia Procurement"
        description="Review, inspect line items, and approve or deny submitted purchase requisitions."
      />
      <PageBreadcrumb pageTitle="Requisition Approvals Portal" />

      <div className="mt-6">
        <RequisitionApprovalsTable />
      </div>
    </>
  );
}
