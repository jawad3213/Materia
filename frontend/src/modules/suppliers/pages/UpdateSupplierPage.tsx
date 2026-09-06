import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import UpdateSupplierForm from '../components/UpdateSupplierForm';

export default function UpdateSupplierPage() {
  return (
    <div>
      <PageBreadcrumb 
        pageTitle="Update Supplier" 
        parentName="Suppliers" 
        parentUrl="/suppliers" 
      />
      <div className="mt-6 flex flex-col gap-6">
        <UpdateSupplierForm />
      </div>
    </div>
  );
}
