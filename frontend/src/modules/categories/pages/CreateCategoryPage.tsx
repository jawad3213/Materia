import React from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import CreateCategoryForm from "../components/CreateCategoryForm";

export default function CreateCategoryPage() {
  return (
    <>
      <PageMeta
        title="Create Category | Materia Admin"
        description="This is the Create Category page for Materia Admin"
      />
      <PageBreadcrumb 
        pageTitle="Create Category" 
        parentName="Categories" 
        parentUrl="/categories" 
      />

      <CreateCategoryForm />
    </>
  );
}
