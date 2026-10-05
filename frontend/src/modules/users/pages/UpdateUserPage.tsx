import { useParams } from 'react-router-dom';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import UpdateUserForm from '../components/UpdateUserForm';
import { PageNotFound } from '../../../shared/components/page/DetailParts';

export default function UpdateUserPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <PageNotFound title="Employee Not Found" message="The link has no employee." backTo="/users" backLabel="Back to Staff Directory" />;
  }

  return (
    <>
      <PageMeta title="Edit Employee | Materia Dashboard" description="Update an employee's profile and employment details" />
      <PageBreadcrumb pageTitle="Edit Employee" parentName="Staff Directory" parentUrl="/users" />
      <UpdateUserForm id={id} />
    </>
  );
}
