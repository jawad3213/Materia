import React from 'react';
import PageBreadcrumb from '../../../shared/components/common/PageBreadCrumb';
import CreateSupplierForm from '../components/CreateSupplierForm';

export default function CreateSupplierPage() {
  return (
    <div>
      <PageBreadcrumb 
        pageTitle="Create Supplier" 
        parentName="Suppliers" 
        parentUrl="/suppliers" 
      />
      <div className="mt-6 flex flex-col gap-6">
        <CreateSupplierForm />
      </div>
    </div>
  );
}
