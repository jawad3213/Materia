import type { SupplierStatusValue } from '../enums/SupplierStatus';

export interface Supplier {
  id: string;
  code: string;
  name: string;
  description?: string;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  address: string;
  city: string;
  country: string;
  postalCode?: string;
  fullAddress?: string;
  paymentTerms?: string[];
  paymentDelay?: number;
  currencyCode?: string;
  status: SupplierStatusValue;
  createdBy: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
}
