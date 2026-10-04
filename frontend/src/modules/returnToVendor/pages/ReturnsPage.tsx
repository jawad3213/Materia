import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import ReturnList from "../components/ReturnList";

export default function ReturnsPage() {
  return (
    <>
      <PageMeta title="Vendor Returns | Materia Dashboard" description="Rejected goods sent back to suppliers, and their replacement or credit" />
      <PageBreadcrumb pageTitle="Vendor Returns" />
      <div className="space-y-6">
        <ReturnList />
      </div>
    </>
  );
}
