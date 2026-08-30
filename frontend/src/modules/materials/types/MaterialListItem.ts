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
  supplierId: string;
}
