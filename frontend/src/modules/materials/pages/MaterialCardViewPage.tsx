import React, { useEffect, useState } from "react";
import PageBreadcrumb from "../../../shared/components/common/PageBreadCrumb";
import PageMeta from "../../../shared/components/common/PageMeta";
import MaterialCard from "../components/MaterialCard";
import { materialApi } from "../services/materialApi";

export default function MaterialCardViewPage() {
  const [materials, setMaterials] = useState<any[]>([]);
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
      <PageBreadcrumb pageTitle="Material Cards" />

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
            {/* INJECTED MOCK MATERIALS FOR TESTING UI (3 CARDS SIDE-BY-SIDE) */}
            <MaterialCard
              key="mock-test-1"
              material={{
                id: "mock-id-1",
                code: "MAT-9999",
                name: "Ultra-Premium Steel Alloy",
                alternativeName: "Titanium-Infused Steel V2",
                shortDescription: "High-grade steel alloy infused with titanium.",
                description: "This is a detailed description of the Ultra-Premium Steel Alloy. It is primarily used in aerospace engineering and high-stress environments. The titanium infusion provides a 40% increase in tensile strength compared to standard alloys. Note: Requires special handling and storage conditions to prevent oxidation before processing.",
                searchKeywords: "steel, titanium, aerospace",
                materialType: "RAW_MATERIAL",
                status: "ACTIVE",
                categoryId: "cat-metals",
                categoryName: "Industrial Metals",
                supplierId: "sup-123"
              }}
              highlightKeyword="steel"
            />
            <MaterialCard
              key="mock-test-2"
              material={{
                id: "mock-id-2",
                code: "MAT-9998",
                name: "Industrial Copper Wiring",
                alternativeName: "Heavy Duty Cu-Wire",
                shortDescription: "Standard 12AWG copper wire for industrial applications.",
                description: "Premium grade copper wiring designed specifically for high-voltage industrial setups. Excellent conductivity and thermal resistance. Coated in fire-retardant PVC insulation. Suitable for both indoor and outdoor manufacturing environments where robust electrical infrastructure is essential.",
                searchKeywords: "copper, wire, electrical, 12awg",
                materialType: "SEMI_FINISHED",
                status: "PENDING",
                categoryId: "cat-elec",
                categoryName: "Electrical Supplies",
                supplierId: "sup-124"
              }}
            />
            <MaterialCard
              key="mock-test-3"
              material={{
                id: "mock-id-3",
                code: "MAT-9997",
                name: "Chemical Solvent X-70",
                alternativeName: "Industrial Degreaser",
                shortDescription: "Powerful solvent for cleaning heavy machinery.",
                description: "Industrial strength degreaser and solvent. Highly effective at removing heavy carbon deposits, oil, and grease from manufacturing equipment. Must be used in well-ventilated areas. Operators are required to wear full PPE including respiratory protection. Highly flammable.",
                searchKeywords: "solvent, cleaning, chemical, degreaser",
                materialType: "CONSUMABLE",
                status: "DISCONTINUED",
                categoryId: "cat-chem",
                categoryName: "Chemicals",
                supplierId: "sup-125"
              }}
            />
            {materials.map((material) => (
              <MaterialCard key={material.id} material={material} />
            ))}
          </div>
        )}
      </div>
    </>
  );
}
