import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderForm from "../components/PurchaseOrderForm";

export default function CreatePurchaseOrderPage() {
  return (
    <>
      <PageMeta
        title="Créer un Bon de Commande | Materia Procurement"
        description="Créez un nouveau bon de commande pour vos fournisseurs et précisez les lignes d'articles et conditions de livraison."
      />
      <PageBreadcrumb
        pageTitle="Nouveau Bon de Commande"
        parentName="Bons de Commande"
        parentUrl="/purchase-orders"
      />

      <div className="mt-6">
        <PurchaseOrderForm />
      </div>
    </>
  );
}
