export interface MaterialListItem {
  id: string;
  code: string;
  name: string;
  shortDescription?: string;
  materialType: string;
  status: string;
  unitOfMeasure: string;
  currentStock: number;
  availableStock?: number;
  minimumStock?: number;
  maximumStock?: number;
  reorderPoint?: number;
  safetyStock?: number;
  stockStatus?: "IN_STOCK" | "REORDER_NEEDED" | "CRITICAL" | "OUT_OF_STOCK" | string;
  stockOnOrder?: number;
  virtualStock?: number;
  standardPrice: number | string;
  standardPriceCurrency: string;
  categoryId: string;
  categoryName?: string;
  supplierId: string;
  description?: string;
  alternativeName?: string;
  searchKeywords?: string;
}
