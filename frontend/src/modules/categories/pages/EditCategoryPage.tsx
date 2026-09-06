import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import PageMeta from "../../../shared/components/common/PageMeta";
import EditCategoryForm from '../components/EditCategoryForm';

export default function EditCategoryPage() {
  return (
    <>
      <PageMeta
        title="Edit Category | Materia Admin"
        description="Edit an existing category"
      />
      <PageBreadcrumb 
        pageTitle="Edit Category" 
        parentName="Categories" 
        parentUrl="/categories" 
      />
      <div className="mt-6 flex flex-col gap-6">
        <EditCategoryForm />
      </div>
    </>
  );
}
