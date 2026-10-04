import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import ReturnForm from "../components/ReturnForm";

export default function CreateReturnPage() {
  return (
    <>
      <PageMeta title="New Return | Materia Dashboard" description="Send goods rejected at receipt back to the supplier" />
      <PageBreadcrumb pageTitle="New Return" parentName="Vendor Returns" parentUrl="/returns" />
      <ReturnForm />
    </>
  );
}
