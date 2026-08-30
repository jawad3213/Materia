export interface MaterialSearchRequest {
  keyword?: string;
  categoryId?: string;
  supplierId?: string;
  status?: string;
  minPrice?: number;
  maxPrice?: number;
  lowStockOnly?: boolean;
}
