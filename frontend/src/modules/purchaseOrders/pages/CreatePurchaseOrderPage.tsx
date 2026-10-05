import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderForm from "../components/PurchaseOrderForm";

export default function CreatePurchaseOrderPage() {
  return (
    <>
      <PageMeta title="New Purchase Order | Materia Dashboard" description="Order materials from a supplier" />
      <PageBreadcrumb pageTitle="New Purchase Order" parentName="Purchase Orders" parentUrl="/purchase-orders" />
      <PurchaseOrderForm />
    </>
  );
}
