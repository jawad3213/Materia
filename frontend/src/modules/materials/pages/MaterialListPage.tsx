import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import MaterialListTable from "../components/MaterialListTable";

export default function MaterialListPage() {
  return (
    <>
      <PageMeta
        title="Materials | Materia Admin"
        description="View and manage all your materials"
      />
      <PageBreadcrumb pageTitle="Materials" />
      
      <div className="mt-6">
        <MaterialListTable />
      </div>
    </>
  );
}
