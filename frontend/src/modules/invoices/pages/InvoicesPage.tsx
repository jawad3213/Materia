import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceList from "../components/InvoiceList";

export default function InvoicesPage() {
  return (
    <>
      <PageMeta
        title="Factures | Materia Procurement"
        description="Suivez les factures fournisseurs, leur vérification et leur paiement."
      />
      <PageBreadcrumb pageTitle="Factures Fournisseurs" />

      <div className="mt-6">
        <InvoiceList />
      </div>
    </>
  );
}
