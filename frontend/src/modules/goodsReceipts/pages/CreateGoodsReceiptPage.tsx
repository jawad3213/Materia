import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptForm from "../components/GoodsReceiptForm";

export default function CreateGoodsReceiptPage() {
  return (
    <>
      <PageMeta title="Create Goods Receipt | Materia Dashboard" description="Record received and rejected quantities for a purchase order" />
      <PageBreadcrumb pageTitle="Create Goods Receipt" parentName="Goods Receipts" parentUrl="/goods-receipts" />
      <GoodsReceiptForm />
    </>
  );
}
