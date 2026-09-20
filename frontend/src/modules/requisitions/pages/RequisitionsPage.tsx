import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import RequisitionListTable from "../components/RequisitionListTable";

export default function RequisitionsPage() {
  return (
    <>
      <PageMeta
        title="Purchase Requisitions | Materia Procurement"
        description="Manage purchase requisitions, review line items, and convert approved requisitions to purchase orders."
      />
      <PageBreadcrumb pageTitle="Purchase Requisitions" />

      <div className="mt-6">
        <RequisitionListTable />
      </div>
    </>
  );
}
