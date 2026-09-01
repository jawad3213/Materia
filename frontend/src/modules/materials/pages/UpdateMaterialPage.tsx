import React from "react";
import { useParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import UpdateMaterialForm from "../components/UpdateMaterialForm";

export default function UpdateMaterialPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <p className="p-6 text-error-500">Material ID is missing from the URL.</p>;
  }

  return (
    <>
      <PageMeta
        title="Update Material | Materia Admin"
        description="Edit and update an existing material"
      />
      <PageBreadcrumb pageTitle="Update Material" />

      <div className="mt-6">
        <UpdateMaterialForm id={id} />
      </div>
    </>
  );
}
