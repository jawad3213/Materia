import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceDetail from "../components/InvoiceDetail";

export default function InvoiceDetailPage() {
  return (
    <>
      <PageMeta
        title="Détail Facture | Materia Procurement"
        description="Rapprochement de la facture avec la commande et les réceptions, vérification et paiement."
      />
      <PageBreadcrumb pageTitle="Détail de la Facture" parentName="Factures" parentUrl="/invoices" />

      <div className="mt-6">
        <InvoiceDetail />
      </div>
    </>
  );
}
