import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionDetail from "../components/RequisitionDetail";

export default function RequisitionDetailPage() {
  return (
    <>
      <PageMeta
        title="Requisition Details | Materia Procurement"
        description="Inspect purchase requisition details, audit trail, line items, and lifecycle actions."
      />
      <PageBreadcrumb
        pageTitle="Requisition Details"
        parentName="Purchase Requisitions"
        parentUrl="/requisitions"
      />

      <div className="mt-6">
        <RequisitionDetail />
      </div>
    </>
  );
}
