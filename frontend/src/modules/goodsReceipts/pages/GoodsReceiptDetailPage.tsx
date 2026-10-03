import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptDetail from "../components/GoodsReceiptDetail";

export default function GoodsReceiptDetailPage() {
  return (
    <>
      <PageMeta
        title="Détail Réception | Materia Procurement"
        description="Détail des quantités reçues, du contrôle qualité et des mouvements de stock."
      />
      <PageBreadcrumb
        pageTitle="Détail de la Réception"
        parentName="Réceptions"
        parentUrl="/goods-receipts"
      />

      <div className="mt-6">
        <GoodsReceiptDetail />
      </div>
    </>
  );
}
