import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import InvoiceList from "../components/InvoiceList";

export default function InvoicesPage() {
  return (
    <>
      <PageMeta title="Invoices | Materia Dashboard" description="Supplier invoices, their verification and payment" />
      <PageBreadcrumb pageTitle="Invoices" />
      <div className="space-y-6">
        <InvoiceList />
      </div>
    </>
  );
}
