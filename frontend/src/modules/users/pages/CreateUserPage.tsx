import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import CreateUserForm from '../components/CreateUserForm';

export default function CreateUserPage() {
  return (
    <>
      <PageMeta
        title="Onboard Employee | Materia ERP"
        description="Onboard a new employee and configure system login credentials"
      />
      <PageBreadcrumb
        pageTitle="Onboard Employee"
        parentName="Staff & Users"
        parentUrl="/users"
      />

      <div className="mt-6">
        <CreateUserForm />
      </div>
    </>
  );
}
