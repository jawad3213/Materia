import type { SupplierStatusValue } from '../enums/SupplierStatus';

export interface UpdateSupplierRequest {
  name?: string;
  description?: string;
  contactPerson?: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
  paymentTerms?: string[];
  paymentDelay?: number;
  currencyCode?: string;
  status?: SupplierStatusValue;
  updatedBy?: string;
}