import { useParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderForm from "../components/PurchaseOrderForm";

export default function EditPurchaseOrderPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <>
      <PageMeta
        title="Modifier un Bon de Commande | Materia Procurement"
        description="Modifiez un bon de commande tant qu'il n'a pas été confirmé par le fournisseur."
      />
      <PageBreadcrumb
        pageTitle="Modifier le Bon de Commande"
        parentName="Bons de Commande"
        parentUrl="/purchase-orders"
      />

      <div className="mt-6">
        <PurchaseOrderForm key={id} purchaseOrderId={id} />
      </div>
    </>
  );
}
