export interface MaterialListItem {
  id: string;
  code: string;
  name: string;
  shortDescription: string;
  materialType: string;
  status: string;
  unitOfMeasure: string;
  currentStock: number;
  standardPrice: number;
  standardPriceCurrency: string;
  categoryId: string;
  categoryName?: string;
  supplierId: string;
  description?: string;
  alternativeName?: string;
  searchKeywords?: string;
}
