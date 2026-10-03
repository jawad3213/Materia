import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptForm from "../components/GoodsReceiptForm";

export default function CreateGoodsReceiptPage() {
  return (
    <>
      <PageMeta
        title="Nouvelle Réception | Materia Procurement"
        description="Enregistrez les quantités reçues et rejetées pour une commande fournisseur."
      />
      <PageBreadcrumb
        pageTitle="Nouvelle Réception"
        parentName="Réceptions"
        parentUrl="/goods-receipts"
      />

      <div className="mt-6">
        <GoodsReceiptForm />
      </div>
    </>
  );
}
