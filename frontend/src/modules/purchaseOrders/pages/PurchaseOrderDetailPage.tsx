import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderDetail from "../components/PurchaseOrderDetail";

export default function PurchaseOrderDetailPage() {
  return (
    <>
      <PageMeta
        title="Détails du Bon de Commande | Materia Procurement"
        description="Inspectez les articles, les conditions de paiement et gérez le workflow du bon de commande."
      />
      <PageBreadcrumb
        pageTitle="Détails du Bon de Commande"
        parentName="Bons de Commande"
        parentUrl="/purchase-orders"
      />

      <div className="mt-6">
        <PurchaseOrderDetail />
      </div>
    </>
  );
}
