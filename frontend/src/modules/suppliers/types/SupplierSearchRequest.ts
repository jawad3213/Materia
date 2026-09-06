import type { SupplierStatusValue } from '../enums/SupplierStatus';

export interface SupplierSearchRequest {
  // ---- Search fields ----
  code?: string;
  name?: string;
  description?: string;
  contactPerson?: string;
  contactEmail?: string;
  fullAddress?: string;

  // ---- Filter fields ----
  status?: SupplierStatusValue | string;
  currencyCode?: string;
  country?: string;
}
