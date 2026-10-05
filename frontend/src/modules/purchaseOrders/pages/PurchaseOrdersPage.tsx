import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PurchaseOrderList from "../components/PurchaseOrderList";

export default function PurchaseOrdersPage() {
  return (
    <>
      <PageMeta title="Purchase Orders | Materia Dashboard" description="Supplier orders, their delivery and their receipt" />
      <PageBreadcrumb pageTitle="Purchase Orders" />
      <div className="space-y-6">
        <PurchaseOrderList />
      </div>
    </>
  );
}
