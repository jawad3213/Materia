import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import UserListTable from '../components/UserListTable';

export default function UsersPage() {
  return (
    <>
      <PageMeta
        title="Staff & User Management | Materia ERP"
        description="Manage company employees, job roles, system access, and lifecycle onboarding/offboarding."
      />
      <PageBreadcrumb pageTitle="Staff & Users" />

      <div className="mt-6">
        <UserListTable />
      </div>
    </>
  );
}
