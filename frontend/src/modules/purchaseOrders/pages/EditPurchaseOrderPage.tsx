import { useParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderForm from "../components/PurchaseOrderForm";

export default function EditPurchaseOrderPage() {
  const { id } = useParams<{ id: string }>();

  return (
    <>
      <PageMeta title="Edit Purchase Order | Materia Dashboard" description="Change an order until the supplier confirms it" />
      <PageBreadcrumb pageTitle="Edit Purchase Order" parentName="Purchase Orders" parentUrl="/purchase-orders" />
      <PurchaseOrderForm key={id} purchaseOrderId={id} />
    </>
  );
}
