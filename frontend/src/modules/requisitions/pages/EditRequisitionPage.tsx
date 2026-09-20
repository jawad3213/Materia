import React from "react";
import { useParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateRequisitionForm from "../components/CreateRequisitionForm";

export default function EditRequisitionPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <p className="p-6 text-red-500">Requisition ID is missing from the URL.</p>;
  }

  return (
    <>
      <PageMeta
        title="Edit Purchase Requisition | Materia Procurement"
        description="Modify purchase requisition details, adjust quantities, and update procurement items."
      />
      <PageBreadcrumb
        pageTitle="Edit Requisition"
        parentName="Purchase Requisitions"
        parentUrl="/purchase-requisitions"
      />

      <div className="mt-6">
        <CreateRequisitionForm requisitionId={id} />
      </div>
    </>
  );
}
