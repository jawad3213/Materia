import { useParams } from "react-router-dom";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateRequisitionForm from "../components/CreateRequisitionForm";
import { PageNotFound } from "../../../shared/components/page/DetailParts";

export default function EditRequisitionPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <PageNotFound title="Requisition Not Found" message="The link has no requisition." backTo="/requisitions" backLabel="Back to Requisitions" />;
  }

  return (
    <>
      <PageMeta title="Edit Requisition | Materia Dashboard" description="Change a requisition until it is approved" />
      <PageBreadcrumb pageTitle="Edit Requisition" parentName="Requisitions" parentUrl="/requisitions" />
      <CreateRequisitionForm requisitionId={id} />
    </>
  );
}
