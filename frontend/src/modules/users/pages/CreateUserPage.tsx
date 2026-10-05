import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import CreateUserForm from '../components/CreateUserForm';

export default function CreateUserPage() {
  return (
    <>
      <PageMeta title="Onboard Employee | Materia Dashboard" description="Add an employee and, if needed, a system login" />
      <PageBreadcrumb pageTitle="Onboard Employee" parentName="Staff Directory" parentUrl="/users" />
      <CreateUserForm />
    </>
  );
}
