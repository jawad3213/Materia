import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import GoodsReceiptList from "../components/GoodsReceiptList";

export default function GoodsReceiptsPage() {
  return (
    <>
      <PageMeta
        title="Réceptions | Materia Procurement"
        description="Suivez les réceptions de marchandises, les quantités acceptées et les rejets qualité."
      />
      <PageBreadcrumb pageTitle="Réceptions de Marchandises" />

      <div className="mt-6">
        <GoodsReceiptList />
      </div>
    </>
  );
}
