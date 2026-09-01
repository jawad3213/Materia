export interface CreateMaterialRequest {
  name: string;
  description?: string;
  shortDescription?: string;
  searchKeywords?: string;
  alternativeName?: string;
  categoryId: string;
  supplierId: string;
  materialType: string;
  status?: string;
  unitOfMeasure: string;
  currentStock: number;
  minimumStock: number;
  maximumStock: number;
  reorderPoint?: number;
  safetyStock: number;
  economicOrderQuantity?: number;
  standardPrice: number;
  standardPriceCurrency: string;
  costPrice?: number;
  costPriceCurrency?: string;
  createdBy: string;
}
