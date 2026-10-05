import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateRequisitionForm from "../components/CreateRequisitionForm";

export default function CreateRequisitionPage() {
  return (
    <>
      <PageMeta title="New Requisition | Materia Dashboard" description="Request materials and send the request for approval" />
      <PageBreadcrumb pageTitle="New Requisition" parentName="Requisitions" parentUrl="/requisitions" />
      <CreateRequisitionForm />
    </>
  );
}
