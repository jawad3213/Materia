import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import UserListTable from '../components/UserListTable';

export default function UsersPage() {
  return (
    <>
      <PageMeta title="Staff Directory | Materia Dashboard" description="Employees, their employment status and system access" />
      <PageBreadcrumb pageTitle="Staff Directory" />
      <div className="space-y-6">
        <UserListTable />
      </div>
    </>
  );
}
