export interface Material {
  id: string;
  code: string;
  name: string;
  description?: string;
  shortDescription?: string;
  searchKeywords?: string;
  alternativeName?: string;
  categoryId: string;
  categoryName?: string;
  supplierId: string;
  supplierName?: string;
  materialType: string;
  status: string;
  unitOfMeasure: string;
  currentStock: number;
  availableStock: number;
  minimumStock: number;
  maximumStock: number;
  reorderPoint: number;
  safetyStock: number;
  economicOrderQuantity: number;
  standardPrice: string;
  costPrice?: string;
  lastPurchasePrice?: string;
  averagePurchasePrice?: string;
  currencyCode: string;
  isBelowMinimumStock: boolean;
  isReorderNeeded: boolean;
  isOutOfStock: boolean;
  stockStatus?: "IN_STOCK" | "REORDER_NEEDED" | "CRITICAL" | "OUT_OF_STOCK" | string;
  stockOnOrder?: number;
  virtualStock?: number;
  obsoletedAt?: string;
  obsoletedBy?: string;
  obsoletedReason?: string;
  createdBy: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
}

export interface ReorderRecommendation {
  materialId: string;
  materialCode: string;
  materialName: string;
  currentStock: number;
  stockOnOrder: number;
  virtualStock: number;
  reorderPoint: number;
  safetyStock: number;
  recommendedQuantity: number;
  estimatedCost: number;
  currency: string;
  reason: string;
  isUrgent: boolean;
  stockStatus: string;
}

export interface ManualReorderResponse {
  materialId: string;
  materialCode: string;
  purchaseRequisitionId: string;
  quantityReordered: number;
  message: string;
}
