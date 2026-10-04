import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import PaymentList from "../components/PaymentList";

export default function PaymentsPage() {
  return (
    <>
      <PageMeta title="Payments | Materia Dashboard" description="Supplier payments and their execution" />
      <PageBreadcrumb pageTitle="Payments" />
      <div className="space-y-6">
        <PaymentList />
      </div>
    </>
  );
}
