import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateMaterialForm from "../components/CreateMaterialForm";

export default function CreateMaterialPage() {
  return (
    <>
      <PageMeta
        title="Create Material | Materia Admin"
        description="This is the Create Material page for Materia Admin"
      />
      <PageBreadcrumb pageTitle="Create Material" />

      <CreateMaterialForm />
    </>
  );
}
