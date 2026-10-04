import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceForm from "../components/InvoiceForm";

export default function CreateInvoicePage() {
  return (
    <>
      <PageMeta
        title="Nouvelle Facture | Materia Procurement"
        description="Saisissez une facture fournisseur à partir d'une commande réceptionnée."
      />
      <PageBreadcrumb pageTitle="Nouvelle Facture" parentName="Factures" parentUrl="/invoices" />

      <div className="mt-6">
        <InvoiceForm />
      </div>
    </>
  );
}
