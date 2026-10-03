import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderList from "../components/PurchaseOrderList";

export default function PurchaseOrdersPage() {
  return (
    <>
      <PageMeta
        title="Bons de Commande | Materia Procurement"
        description="Gérez les bons de commande d'achat, suivez les expéditions fournisseurs et assignez les réceptions au magasin."
      />
      <PageBreadcrumb pageTitle="Bons de Commande Fournisseurs" />

      <div className="mt-6">
        <PurchaseOrderList />
      </div>
    </>
  );
}
