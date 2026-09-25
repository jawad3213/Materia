import React from 'react';
import { useParams } from 'react-router-dom';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from '../../../shared/components/common/PageMeta';
import UpdateUserForm from '../components/UpdateUserForm';

export default function UpdateUserPage() {
  const { id } = useParams<{ id: string }>();

  if (!id) {
    return <p className="p-6 text-error-500">Employee ID is missing from URL.</p>;
  }

  return (
    <>
      <PageMeta
        title="Edit Employee | Materia ERP"
        description="Update employee profile and employment details"
      />
      <PageBreadcrumb
        pageTitle="Edit Employee"
        parentName="Staff & Users"
        parentUrl="/users"
      />

      <div className="mt-6">
        <UpdateUserForm id={id} />
      </div>
    </>
  );
}
