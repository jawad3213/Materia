import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptDetail from "../components/GoodsReceiptDetail";

/** The detail component renders its own breadcrumb and actions, as the supplier detail page does. */
export default function GoodsReceiptDetailPage() {
  return (
    <>
      <PageMeta title="Goods Receipt | Materia Dashboard" description="Received quantities, quality control and stock movements" />
      <GoodsReceiptDetail />
    </>
  );
}
