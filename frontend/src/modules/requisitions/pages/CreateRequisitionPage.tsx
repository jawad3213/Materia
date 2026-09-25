import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateRequisitionForm from "../components/CreateRequisitionForm";

export default function CreateRequisitionPage() {
  return (
    <>
      <PageMeta
        title="Create Purchase Requisition | Materia Procurement"
        description="Initiate a purchase requisition, specify item quantities, and submit for procurement approval."
      />
      <PageBreadcrumb
        pageTitle="Create Requisition"
        parentName="Purchase Requisitions"
        parentUrl="/requisitions"
      />

      <div className="mt-6">
        <CreateRequisitionForm />
      </div>
    </>
  );
}
