import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PaymentForm from "../components/PaymentForm";

export default function CreatePaymentPage() {
  return (
    <>
      <PageMeta title="Create Payment | Materia Dashboard" description="Pay one or more verified invoices of a supplier" />
      <PageBreadcrumb pageTitle="Create Payment" parentName="Payments" parentUrl="/payments" />
      <PaymentForm />
    </>
  );
}
