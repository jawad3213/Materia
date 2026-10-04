import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptList from "../components/GoodsReceiptList";

export default function GoodsReceiptsPage() {
  return (
    <>
      <PageMeta title="Goods Receipts | Materia Dashboard" description="Track deliveries, accepted quantities and quality rejections" />
      <PageBreadcrumb pageTitle="Goods Receipts" />
      <div className="space-y-6">
        <GoodsReceiptList />
      </div>
    </>
  );
}
