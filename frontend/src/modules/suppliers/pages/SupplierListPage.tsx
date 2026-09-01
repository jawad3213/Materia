import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import SupplierListTable from "../components/SupplierListTable";

export default function SupplierListPage() {
  return (
    <>
      <PageMeta
        title="Suppliers | Materia Dashboard"
        description="Manage your suppliers in Materia"
      />
      <PageBreadcrumb pageTitle="Suppliers" />
      <div className="space-y-6">
        <SupplierListTable />
      </div>
    </>
  );
}
