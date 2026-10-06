import React, { useEffect, useState } from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import MaterialCard from "../components/MaterialCard";
import { materialApi } from "../services/materialApi";
import type { MaterialListItem } from "../types/MaterialListItem";

export default function MaterialCardViewPage() {
  const [materials, setMaterials] = useState<MaterialListItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMaterials = async () => {
      try {
        setLoading(true);
        const res = await materialApi.getAll(0, 10);
        setMaterials(res.data.content || []);
      } catch (err) {
        console.error("Failed to load materials:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchMaterials();
  }, []);

  return (
    <>
      <PageMeta
        title="Material Cards | Materia Admin"
        description="View materials as cards"
      />
      <PageBreadcrumb 
        pageTitle="Material Cards" 
        parentName="Materials" 
        parentUrl="/materials" 
      />

      <div className="mt-6">
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="flex flex-col items-center gap-3">
              <svg className="animate-spin h-8 w-8 text-brand-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <p className="text-sm text-gray-500 dark:text-gray-400">Loading materials...</p>
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-5 xl:grid-cols-3">
            {materials.map((material) => (
              <MaterialCard key={material.id} material={material} />
            ))}
          </div>
        )}
      </div>
    </>
  );
}
