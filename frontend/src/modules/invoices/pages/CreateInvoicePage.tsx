import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceForm from "../components/InvoiceForm";

export default function CreateInvoicePage() {
  return (
    <>
      <PageMeta title="Record Invoice | Materia Dashboard" description="Record a supplier invoice from a received purchase order" />
      <PageBreadcrumb pageTitle="Record Invoice" parentName="Invoices" parentUrl="/invoices" />
      <InvoiceForm />
    </>
  );
}
