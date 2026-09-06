import type { SupplierStatusValue } from '../enums/SupplierStatus';

export interface SupplierFilterRequest {
  status?: SupplierStatusValue | string;
  currencyCode?: string;
  country?: string;
}
